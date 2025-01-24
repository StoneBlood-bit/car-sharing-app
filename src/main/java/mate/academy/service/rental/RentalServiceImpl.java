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
import mate.academy.repository.CarRepository;
import mate.academy.repository.RentalRepository;
import mate.academy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    private Logger logger = LoggerFactory.getLogger(RentalServiceImpl.class);

    @Override
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

        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public List<RentalDetailDto> getRentals(RentalFilterRequestDto filter, User currentUser) {
        logger.info("Fetching rentals for user: {}, role: {}, filter: userId={}, isActive={}",
                currentUser.getEmail(), currentUser.getRole(), filter.getUserId(),
                filter.getIsActive());

        List<Rental> rentals;

        if (currentUser.isAdmin()) {
            logger.info("User is admin, "
                    + "fetching all rentals or filtering based on userId and isActive");
            if (filter.getUserId() != null && filter.getIsActive() != null) {
                logger.info("Filter by userId={} and isActive={}",
                        filter.getUserId(), filter.getIsActive());
                if (filter.getIsActive()) {
                    rentals = rentalRepository.findActiveRentalsByUserId(filter.getUserId());
                } else {
                    rentals = rentalRepository.findCompletedRentalsByUserId(filter.getUserId());
                }
            } else if (filter.getUserId() != null) {
                logger.info("Filter by userId={}", filter.getUserId());
                rentals = rentalRepository.findByUserId(filter.getUserId());
            } else if (filter.getIsActive() != null) {
                logger.info("Filter by isActive={}", filter.getIsActive());
                rentals = filter.getIsActive()
                        ? rentalRepository.findActiveRentals()
                        : rentalRepository.findCompletedRentals();
            } else {
                logger.info("No filters applied, fetching all rentals");
                rentals = rentalRepository.findAll();
            }
        } else {
            logger.info("User is not admin, fetching rentals for userId={}", currentUser.getId());
            if (filter.getIsActive() != null) {
                logger.info("Filter by isActive={}", filter.getIsActive());
                rentals = filter.getIsActive()
                        ? rentalRepository.findActiveRentalsByUserId(currentUser.getId())
                        : rentalRepository.findCompletedRentalsByUserId(currentUser.getId());
            } else {
                logger.info("Fetching rentals for current user only");
                rentals = rentalRepository.findByUserId(currentUser.getId());
            }
        }

        logger.info("Fetched {} rentals from repository", rentals.size());
        return rentals.stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public void completeRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );

        if (rental.getActualReturnDate() != null) {
            throw new RuntimeException("Rental has already been completed.");
        }

        rental.setActualReturnDate(LocalDateTime.now());
        rentalRepository.save(rental);

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);

        carRepository.save(car);
    }

    @Override
    public RentalDetailDto getRentalById(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findByIdAndUserId(rentalId, userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: " + rentalId)
        );

        return rentalMapper.toDto(rental);
    }
}
