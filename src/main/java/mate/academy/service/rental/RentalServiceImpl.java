package mate.academy.service.rental;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalDetailDto createRental(RentalRequestDto requestDto, String email) {
        Car car = carRepository.findById(requestDto.getCarId()).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find car with id: " + requestDto.getCarId()
                )
        );

        if (car.getInventory() <= 0) {
            throw new EntityNotFoundException("Car is not available for rental");
        }

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with email: " + email)
        );

        Rental rental = new Rental();
        rental.setRentalDate(requestDto.getRentalDate());
        rental.setReturnDate(requestDto.getReturnDate());
        rental.setCar(car);
        rental.setUser(user);

        Rental saved = rentalRepository.save(rental);

        notificationService.sendNewRentalNotification(saved);

        return rentalMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<RentalDetailDto> getRentals(RentalFilterRequestDto filter, User currentUser) {
        List<Rental> rentals;

        if (currentUser.isAdmin()) {
            rentals = getRentalsForManager(filter);
        } else {
            rentals = getRentalsForCustomer(filter, currentUser);
        }

        return rentals.stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void completeRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );

        if (rental.getActualReturnDate() != null) {
            throw new RuntimeException("Rental has already been completed.");
        }

        notificationService.sendReturnRentalNotification(rental);

        rental.setActualReturnDate(LocalDateTime.now());
        rentalRepository.save(rental);

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);

        carRepository.save(car);
    }

    @Override
    @Transactional
    public RentalDetailDto getRentalById(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findByIdAndUserId(rentalId, userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );

        return rentalMapper.toDto(rental);
    }

    public List<Rental> getRentalsForManager(RentalFilterRequestDto filter) {
        if (filter.getUserId() != null && filter.getIsActive() != null) {
            if (filter.getIsActive()) {
                return rentalRepository.findActiveRentalsByUserId(filter.getUserId());
            } else {
                return rentalRepository.findCompletedRentalsByUserId(filter.getUserId());
            }
        } else if (filter.getUserId() != null) {
            return rentalRepository.findByUserId(filter.getUserId());
        } else if (filter.getIsActive() != null) {
            return filter.getIsActive()
                    ? rentalRepository.findActiveRentals()
                    : rentalRepository.findCompletedRentals();
        } else {
            return rentalRepository.findAll();
        }
    }

    private List<Rental> getRentalsForCustomer(RentalFilterRequestDto filter, User currentUser) {
        if (filter.getIsActive() != null) {
            return filter.getIsActive()
                    ? rentalRepository.findActiveRentalsByUserId(currentUser.getId())
                    : rentalRepository.findCompletedRentalsByUserId(currentUser.getId());
        } else {
            return rentalRepository.findByUserId(currentUser.getId());
        }
    }
}
