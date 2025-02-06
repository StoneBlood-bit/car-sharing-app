package mate.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mate.academy.dto.car.CarDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.CarMapper;
import mate.academy.model.Car;
import mate.academy.repository.CarRepository;
import mate.academy.service.car.CarServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    @InjectMocks
    private CarServiceImpl carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Test
    @DisplayName("Save book with valid data")
    void save_ValidCarDto_ShouldReturnCarDto() {
        CarDto carDto = new CarDto();

        carDto.setModel("X5");
        carDto.setBrand("BMW");
        carDto.setType("SUV");
        carDto.setInventory(12);
        carDto.setDailyFee(BigDecimal.valueOf(120.99));

        Car car = new Car();
        car.setModel(carDto.getModel());
        car.setBrand(carDto.getBrand());
        car.setType(Car.Type.SUV);
        car.setInventory(carDto.getInventory());
        car.setDailyFee(carDto.getDailyFee());

        when(carMapper.toModel(carDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        CarDto savedCar = carService.save(carDto);

        assertThat(savedCar).isEqualTo(carDto);
        verifyNoMoreInteractions(carMapper, carRepository);
    }

    @Test
    @DisplayName("Find all cars with valid Pageable")
    void findAll_ValidPageable_ShouldReturnPageOfCarDto() {
        Car car = new Car();

        car.setId(1L);
        car.setModel("X5");
        car.setBrand("BMW");
        car.setType(Car.Type.SUV);
        car.setInventory(12);
        car.setDailyFee(BigDecimal.valueOf(120.99));

        CarDto carDto = new CarDto();

        carDto.setId(car.getId());
        carDto.setModel(car.getModel());
        carDto.setBrand(car.getBrand());
        carDto.setType(car.getType().toString());
        carDto.setInventory(car.getInventory());
        carDto.setDailyFee(car.getDailyFee());

        Pageable pageable = PageRequest.of(0, 10);
        List<Car> cars = List.of(car);
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        Page<CarDto> actualPage = carService.findAll(pageable);

        assertThat(actualPage).hasSize(1);
        assertThat(actualPage.getContent().get(0)).isEqualTo(carDto);
        verifyNoMoreInteractions(carMapper, carRepository);
    }

    @Test
    @DisplayName("Find all cars with negative page index")
    void findAll_NegativePageIndex_ShouldThrowException() {
        assertThatThrownBy(() -> PageRequest.of(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page index must not be less than zero");
    }

    @Test
    @DisplayName("Find car by valid id")
    void getById_ValidCarId_ShouldReturnCarDto() {
        Long validCarId = 1L;

        Car car = new Car();
        car.setId(validCarId);
        car.setModel("X5");
        car.setBrand("BMW");
        car.setType(Car.Type.SUV);
        car.setInventory(12);
        car.setDailyFee(BigDecimal.valueOf(120.99));

        CarDto carDto = new CarDto();

        carDto.setId(car.getId());
        carDto.setModel(car.getModel());
        carDto.setBrand(car.getBrand());
        carDto.setType(car.getType().toString());
        carDto.setInventory(car.getInventory());
        carDto.setDailyFee(car.getDailyFee());

        when(carRepository.findById(validCarId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carDto);

        CarDto actualCarDto = carService.getById(validCarId);

        assertThat(actualCarDto).isEqualTo(carDto);
        verifyNoMoreInteractions(carMapper, carRepository);
    }

    @Test
    @DisplayName("Find car by invalid id")
    void getById_InvalidCarId_ShouldThrowException() {
        Long invalidId = 99L;

        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.getById(invalidId)
        );

        assertThat(exception.getMessage()).isEqualTo("Can't get car by id: " + invalidId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Update a car with a valid id and dto")
    void update_ValidIdAndDto_ShouldReturnCarDto() {
        CarDto carDto = new CarDto();
        carDto.setModel("X5");
        carDto.setBrand("BMW");
        carDto.setType(Car.Type.SUV.toString());
        carDto.setInventory(12);
        carDto.setDailyFee(BigDecimal.valueOf(120.99));

        Long validId = 1L;

        Car existingCar = new Car();
        existingCar.setId(validId);
        existingCar.setModel("X5");
        existingCar.setBrand("BMW");
        existingCar.setType(Car.Type.SUV);
        existingCar.setInventory(12);
        existingCar.setDailyFee(BigDecimal.valueOf(110.99));

        Car updatedCar = new Car();
        updatedCar.setId(validId);
        updatedCar.setModel(carDto.getModel());
        updatedCar.setBrand(carDto.getBrand());
        updatedCar.setType(Car.Type.SUV);
        updatedCar.setInventory(carDto.getInventory());
        updatedCar.setDailyFee(carDto.getDailyFee());

        CarDto updatedCarDto = new CarDto();
        updatedCarDto.setId(validId);
        updatedCarDto.setModel(updatedCar.getModel());
        updatedCarDto.setBrand(updatedCar.getBrand());
        updatedCarDto.setType(updatedCar.getType().toString());
        updatedCarDto.setInventory(updatedCar.getInventory());
        updatedCarDto.setDailyFee(updatedCar.getDailyFee());

        when(carRepository.findById(validId)).thenReturn(Optional.of(existingCar));
        doNothing().when(carMapper).updateCarFromDto(carDto, existingCar);
        when(carRepository.save(existingCar)).thenReturn(updatedCar);
        when(carMapper.toDto(updatedCar)).thenReturn(updatedCarDto);

        CarDto result = carService.update(carDto, validId);

        assertThat(result).isEqualTo(updatedCarDto);
        verifyNoMoreInteractions(carMapper, carRepository);
    }

    @Test
    @DisplayName("Update car by invalid id")
    void update_InvalidId_ShouldThrowException() {
        Long invalidId = 99L;

        when(carRepository.findById(invalidId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.getById(invalidId)
        );

        assertThat(exception.getMessage()).isEqualTo("Can't get car by id: " + invalidId);
        verifyNoMoreInteractions(carRepository);
    }

    @Test
    @DisplayName("Delete car when id = null")
    void deleteById_NullId_ShouldThrowException() {
        assertThatThrownBy(() -> carService.deleteById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Id must not be null!");

        verifyNoInteractions(carRepository);
    }

    @Test
    @DisplayName("Delete car by valid id")
    void deleteById_ValidId_ShouldCallRepository() {
        Long carId = 1L;

        carService.deleteById(carId);

        verify(carRepository, times(1)).deleteById(carId);
        verifyNoMoreInteractions(carRepository);
    }
}
