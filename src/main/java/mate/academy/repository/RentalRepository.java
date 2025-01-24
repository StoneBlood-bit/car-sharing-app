package mate.academy.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);

    Optional<Rental> findByIdAndUserId(Long rentalId, Long userId);

    @Query("SELECT r FROM Rental r WHERE r.actualReturnDate IS NULL")
    List<Rental> findActiveRentals();

    @Query("SELECT r FROM Rental r WHERE r.user.id = :userId AND r.actualReturnDate IS NULL")
    List<Rental> findActiveRentalsByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Rental r WHERE r.user.id = :userId AND r.actualReturnDate IS NOT NULL")
    List<Rental> findCompletedRentalsByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Rental r WHERE r.actualReturnDate IS NOT NULL")
    List<Rental> findCompletedRentals();
}
