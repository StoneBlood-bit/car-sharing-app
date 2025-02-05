package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dto.rental.RentalDetailDto;
import mate.academy.dto.rental.RentalFilterRequestDto;
import mate.academy.dto.rental.RentalRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.RentalMapper;
import mate.academy.model.Car;
import mate.academy.model.Rental;
import mate.academy.model.User;
import mate.academy.notification.NotificationService;
import mate.academy.repository.CarRepository;
import mate.academy.repository.RentalRepository;
import mate.academy.repository.UserRepository;
import mate.academy.service.rental.RentalServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    @InjectMocks
    private RentalServiceImpl rentalService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("Create rental with valid data")
    void createRental_ValidData_ShouldReturnRentalDetailDto() {
        Long carId = 1L;
        Long userId = 1L;

        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(carId);
        requestDto.setUserId(userId);
        requestDto.setRentalDate(LocalDateTime.now());
        requestDto.setReturnDate(LocalDateTime.now().plusDays(5));

        Car car = new Car();
        car.setId(carId);
        car.setInventory(12);

        String email = "bob@gmail.com";

        User user = new User();
        user.setEmail(email);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);
        rental.setRentalDate(requestDto.getRentalDate());
        rental.setReturnDate(requestDto.getReturnDate());

        RentalDetailDto detailDto = new RentalDetailDto();
        detailDto.setRentalDate(rental.getRentalDate());
        detailDto.setReturnDate(rental.getReturnDate());
        detailDto.setUserEmail(email);
        detailDto.setCarModel(car.getModel());

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(detailDto);

        RentalDetailDto actual = rentalService.createRental(requestDto, email);

        assertThat(actual).isEqualTo(detailDto);
        assertThat(car.getInventory()).isEqualTo(11);

        verify(carRepository, times(1)).findById(1L);
        verify(carRepository, times(1)).save(any(Car.class));
        verify(userRepository, times(1)).findByEmail(email);
        verify(rentalRepository, times(1)).save(any(Rental.class));
        verify(notificationService, times(1)).sendNewRentalNotification(any(Rental.class));
    }

    @Test
    @DisplayName("Create rental when car not found")
    void createRentals_CarNotFound_ShouldThrowException() {
        Long carId = 1L;
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(carId);

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.createRental(requestDto, "bob@gmail.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find car with id: " + carId);
    }

    @Test
    @DisplayName("Create rental when car is not available")
    void createRental_CarNotAvailable_ShouldThrowException() {
        Long carId = 1L;
        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(carId);

        Car car = new Car();
        car.setId(carId);
        car.setInventory(0);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        assertThatThrownBy(() -> rentalService.createRental(requestDto, "bob@gmail.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Car is not available for rental");
    }

    @Test
    @DisplayName("Create rental when user not found")
    void createRental_UserNotFound_ShouldThrowException() {
        Long carId = 1L;

        RentalRequestDto requestDto = new RentalRequestDto();
        requestDto.setCarId(carId);

        Car car = new Car();
        car.setId(carId);
        car.setInventory(1);

        String email = "bob@gmail.com";

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.createRental(requestDto, email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Can't find user with email: " + email);
    }

    @Test
    @DisplayName("Get rentals for a Manager")
    void getRentals_ForManager_ShouldReturnListOfRentalDetailDto() {
        User adminUser = new User();
        adminUser.setRole(User.Role.MANAGER);

        RentalFilterRequestDto filter = new RentalFilterRequestDto();

        List<Rental> mockRentals = List.of(new Rental(), new Rental());
        List<RentalDetailDto> expectedDtos = List.of(new RentalDetailDto(), new RentalDetailDto());

        when(rentalRepository.findAll()).thenReturn(mockRentals);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(new RentalDetailDto());

        List<RentalDetailDto> result = rentalService.getRentals(filter, adminUser);

        assertThat(result).hasSize(2);
        verify(rentalMapper, times(2)).toDto(any(Rental.class));
    }

    @Test
    @DisplayName("Get rentals for a Customer")
    void getRentals_ForCustomer_ShouldReturnListOfRentalDetailDto() {
        User customerUser = new User();
        customerUser.setRole(User.Role.CUSTOMER);

        RentalFilterRequestDto filter = new RentalFilterRequestDto();

        List<Rental> mockRentals = List.of(new Rental());
        List<RentalDetailDto> expectedDtos = List.of(new RentalDetailDto());

        when(rentalRepository.findByUserId(customerUser.getId())).thenReturn(mockRentals);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(new RentalDetailDto());

        List<RentalDetailDto> result = rentalService.getRentals(filter, customerUser);

        assertThat(result).hasSize(1);
        verify(rentalMapper, times(1)).toDto(any(Rental.class));
    }

    @Test
    @DisplayName("Complete rental with valid data")
    void completeRental_ValidData_ShouldUpdateAndNotify() {
        Long rentalId = 1L;
        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setActualReturnDate(null);

        Car car = new Car();
        car.setInventory(2);
        rental.setCar(car);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        rentalService.completeRental(rentalId);

        assertNotNull(rental.getActualReturnDate());
        assertEquals(3, car.getInventory());

        verify(notificationService, times(1)).sendReturnRentalNotification(rental);
        verify(rentalRepository, times(1)).save(rental);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Complete rental when rental was already completed")
    void completeRental_RentalAlreadyCompleted_ShouldThrowException() {
        Long rentalId = 1L;
        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setActualReturnDate(LocalDateTime.now());

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            rentalService.completeRental(rentalId);
        });

        assertEquals("Rental has already been completed.", exception.getMessage());

        verify(notificationService, never()).sendReturnRentalNotification(any());
        verify(rentalRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get rental by valid id")
    void getRentalById_ValidId_ShouldReturnRentalDetailDto() {
        Long rentalId = 1L;
        Long userId = 2L;

        Rental rental = new Rental();
        rental.setId(rentalId);

        RentalDetailDto expectedDto = new RentalDetailDto();

        when(rentalRepository.findByIdAndUserId(rentalId, userId)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        RentalDetailDto actualDto = rentalService.getRentalById(rentalId, userId);

        assertNotNull(actualDto);
        assertEquals(expectedDto, actualDto);

        verify(rentalRepository, times(1)).findByIdAndUserId(rentalId, userId);
        verify(rentalMapper, times(1)).toDto(rental);
    }

    @Test
    @DisplayName("Get rental by invalid id")
    void getRentalById_InvalidId_ShouldThrowException() {
        Long rentalId = 1L;
        Long userId = 2L;

        when(rentalRepository.findByIdAndUserId(rentalId, userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            rentalService.getRentalById(rentalId, userId);
        });

        assertEquals("Can't find rental with id: " + rentalId, exception.getMessage());

        verify(rentalRepository, times(1)).findByIdAndUserId(rentalId, userId);
        verify(rentalMapper, never()).toDto(any());
    }
}
