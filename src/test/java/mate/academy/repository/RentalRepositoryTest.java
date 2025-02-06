package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.Rental;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
        scripts = "classpath:database/03-create-cars-users-rentals-for-rental-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
        scripts = "classpath:database/04-delete-cars-users-rentals-for-rental-repository-test.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
public class RentalRepositoryTest {
    @Autowired
    private RentalRepository rentalRepository;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    @DisplayName("Find rentals by valid user id")
    void findByUserId_ValidUserId_ShouldReturnListOfRentals() {
        Long validUserId = 1L;

        List<Rental> actualList = rentalRepository.findByUserId(validUserId);

        assertEquals(3, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(0).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-25 14:30", formatter),
                actualList.get(0).getReturnDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-24 14:30", formatter),
                actualList.get(0).getActualReturnDate()
        );
        assertEquals(1, actualList.get(0).getUser().getId());
        assertEquals(1, actualList.get(0).getCar().getId());
    }

    @Test
    @DisplayName("Find rentals by invalid user id")
    void findByUserId_InvalidUserId_ShouldReturnEmptyList() {
        Long invalidUserId = 999L;

        List<Rental> actualList = rentalRepository.findByUserId(invalidUserId);

        assertEquals(0, actualList.size());
        assertTrue(actualList.isEmpty());
    }

    @Test
    @DisplayName("Find rental by valid rental's id and valid user's id")
    void findByIdAndUserId_ValidRentalIdAndValidUserId_ShouldReturnRental() {
        Long validUserId = 1L;
        Long validRentalId = 1L;

        Rental actualRental = rentalRepository
                .findByIdAndUserId(validRentalId, validUserId).orElseThrow(
                    () -> new EntityNotFoundException("Can't find user")
        );

        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualRental.getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-25 14:30", formatter),
                actualRental.getReturnDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-24 14:30", formatter),
                actualRental.getActualReturnDate()
        );
    }

    @Test
    @DisplayName("Find rental by invalid rental's id and invalid user's id")
    void findByIdAndUserId_InvalidRentalIdAndInvalidUserId_ShouldReturnOptionalEmpty() {
        Long invalidUserId = 999L;
        Long invalidRentalId = 999L;

        assertEquals(
                Optional.empty(),
                rentalRepository.findByIdAndUserId(invalidRentalId, invalidUserId)
        );
    }

    @Test
    @DisplayName("Find rental by valid rental's id and invalid user's id")
    void findByIdAndUserId_ValidRentalIdAndInvalidUserId_ShouldReturnOptionalEmpty() {
        Long invalidUserId = 999L;
        Long invalidRentalId = 1L;

        assertEquals(
                Optional.empty(),
                rentalRepository.findByIdAndUserId(invalidRentalId, invalidUserId)
        );
    }

    @Test
    @DisplayName("Find rental by invalid rental's id and valid user's id")
    void findByIdAndUserId_InvalidRentalIdAndValidUserId_ShouldReturnOptionalEmpty() {
        Long invalidUserId = 1L;
        Long invalidRentalId = 999L;

        assertEquals(
                Optional.empty(),
                rentalRepository.findByIdAndUserId(invalidRentalId, invalidUserId)
        );
    }

    @Test
    @DisplayName("Find active rentals when they are")
    void findActiveRentals_FoundActiveRentals_ShouldReturnListOfRentals() {
        List<Rental> actualList = rentalRepository.findActiveRentals();

        assertEquals(2, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(1).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2026-01-25 14:30", formatter),
                actualList.get(1).getReturnDate()
        );
        assertNull(actualList.get(1).getActualReturnDate());
        assertEquals(1, actualList.get(1).getUser().getId());
        assertEquals(3, actualList.get(1).getCar().getId());
    }

    @Test
    @DisplayName("Find active rentals when they are not")
    @Sql(
            scripts = "classpath:"
                    + "database/04-delete-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:"
                    + "database/03-create-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findActiveRentals_NotFoundActiveRentals_ShouldReturnEmptyList() {
        List<Rental> actualList = rentalRepository.findActiveRentals();

        assertTrue(actualList.isEmpty());
    }

    @Test
    @DisplayName("Find active rentals by valid user id")
    void findActiveRentalsByUserId_ValidUserId_ShouldReturnListOfRentals() {
        Long validUserId = 1L;

        List<Rental> actualList = rentalRepository.findActiveRentalsByUserId(validUserId);

        assertEquals(2, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(1).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2026-01-25 14:30", formatter),
                actualList.get(1).getReturnDate()
        );
        assertNull(actualList.get(1).getActualReturnDate());
        assertEquals(1, actualList.get(1).getUser().getId());
        assertEquals(3, actualList.get(1).getCar().getId());
    }

    @Test
    @DisplayName("Find active rentals by invalid user id")
    void findActiveRentalsByUserId_InvalidUserId_ShouldReturnEmptyList() {
        Long invalidUserId = 999L;

        List<Rental> actualList = rentalRepository.findActiveRentalsByUserId(invalidUserId);
        assertTrue(actualList.isEmpty());
    }

    @Test
    @DisplayName("Find completed rentals by valid user id")
    void findCompletedRentalsByUserId_ValidUserId_ShouldReturnListOfRentals() {
        Long validUserId = 1L;

        List<Rental> actualList = rentalRepository.findCompletedRentalsByUserId(validUserId);

        assertEquals(1, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(0).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-25 14:30", formatter),
                actualList.get(0).getReturnDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-24 14:30", formatter),
                actualList.get(0).getActualReturnDate()
        );
        assertEquals(1, actualList.get(0).getUser().getId());
        assertEquals(1, actualList.get(0).getCar().getId());
    }

    @Test
    @DisplayName("Find completed rentals by invalid user id")
    void findCompletedRentalsByUserId_InvalidUserId_ShouldReturnEmptyList() {
        Long invalidUserId = 999L;

        List<Rental> actualList = rentalRepository.findCompletedRentalsByUserId(invalidUserId);

        assertTrue(actualList.isEmpty());
    }

    @Test
    @DisplayName("Find completed rentals when they are")
    void findCompletedRentals_FoundCompletedRentals_ShouldReturnListOfRentals() {
        List<Rental> actualList = rentalRepository.findCompletedRentals();

        assertEquals(1, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(0).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-25 14:30", formatter),
                actualList.get(0).getReturnDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-24 14:30", formatter),
                actualList.get(0).getActualReturnDate()
        );
        assertEquals(1, actualList.get(0).getUser().getId());
        assertEquals(1, actualList.get(0).getCar().getId());
    }

    @Test
    @DisplayName("Find completed rentals when they are not")
    @Sql(
            scripts = "classpath:"
                    + "database/04-delete-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:"
                    + "database/03-create-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findCompletedRentals_NotFoundCompletedRentals_ShouldReturnEmptyList() {
        List<Rental> actualRentals = rentalRepository.findCompletedRentals();

        assertTrue(actualRentals.isEmpty());
    }

    @Test
    @DisplayName("Find overdue rentals")
    void findByReturnDateBeforeAndActualReturnDateIsNull_Valid_ShouldReturnListOfRentals() {
        LocalDateTime valid = LocalDateTime.now();

        List<Rental> actualList = rentalRepository
                .findByReturnDateBeforeAndActualReturnDateIsNull(valid);

        assertEquals(1, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(0).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-25 14:30", formatter),
                actualList.get(0).getReturnDate()
        );
        assertNull(actualList.get(0).getActualReturnDate());
        assertEquals(2, actualList.get(0).getCar().getId());
        assertEquals(1, actualList.get(0).getUser().getId());

    }

    @Test
    @DisplayName("Find no overdue rentals")
    void findByReturnDateAfterOrActualReturnDateIsNotNull_Valid_ShouldReturnListOfRentals() {
        LocalDateTime valid = LocalDateTime.parse("2025-01-25 14:30", formatter);

        List<Rental> actualList = rentalRepository
                .findByReturnDateAfterOrActualReturnDateIsNotNull(valid);

        assertEquals(2, actualList.size());
        assertEquals(
                LocalDateTime.parse("2025-01-20 14:30", formatter),
                actualList.get(0).getRentalDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-25 14:30", formatter),
                actualList.get(0).getReturnDate()
        );
        assertEquals(
                LocalDateTime.parse("2025-01-24 14:30", formatter),
                actualList.get(0).getActualReturnDate()
        );
        assertEquals(1, actualList.get(0).getUser().getId());
        assertEquals(1, actualList.get(0).getCar().getId());
    }
}
