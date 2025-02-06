package mate.academy.repository;

import java.util.List;
import java.util.Optional;
import mate.academy.model.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = "rental")
    @Query("SELECT p FROM Payment p WHERE p.rental.user.id = :userId")
    List<Payment> findByRentalUserId(@Param("userId") Long userId);

    Optional<Payment> findBySessionId(String sessionId);
}
