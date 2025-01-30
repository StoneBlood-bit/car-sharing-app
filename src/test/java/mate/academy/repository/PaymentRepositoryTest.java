package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.model.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Sql(
        scripts = "classpath:database/05-create-payments.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
        scripts = "classpath:database/06-delete-payments.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Find rental by valid user id")
    void findByRentalUserId_ValidUserId_ShouldReturnListOfPayments() {
        Long validUserId = 1L;

        List<Payment> actualList = paymentRepository.findByRentalUserId(validUserId);

        assertEquals(1, actualList.size());
        assertEquals(1, actualList.get(0).getId());
        assertEquals(Payment.Status.PENDING, actualList.get(0).getStatus());
        assertEquals(Payment.Type.PAYMENT, actualList.get(0).getType());
        assertEquals("url.example", actualList.get(0).getSessionUrl());
        assertEquals("sessionId", actualList.get(0).getSessionId());
        assertEquals(BigDecimal.valueOf(1000.99), actualList.get(0).getAmount());
    }

    @Test
    @DisplayName("Find rental by invalid user id")
    void findByRentalUserId_InvalidUserId_ShouldReturnEmptyList() {
        Long invalidUserId = 999L;

        List<Payment> actualList = paymentRepository.findByRentalUserId(invalidUserId);

        assertTrue(actualList.isEmpty());
    }

    @Test
    @DisplayName("Find rental by valid session id")
    void findBySessionId_ValidSessionId_ShouldReturnPayment() {
        String validSessionId = "sessionId";

        Payment actual = paymentRepository.findBySessionId(validSessionId).orElseThrow(
                () -> new EntityNotFoundException("")
        );

        assertEquals(1, actual.getId());
        assertEquals(Payment.Status.PENDING, actual.getStatus());
        assertEquals(Payment.Type.PAYMENT, actual.getType());
        assertEquals("url.example", actual.getSessionUrl());
        assertEquals(BigDecimal.valueOf(1000.99), actual.getAmount());
    }

    @Test
    @DisplayName("Find rental by invalid session id")
    void findBySessionId_InvalidSessionId_ShouldReturnOptionalEmpty() {
        String invalidSessionId = "invalidId";

        assertEquals(Optional.empty(), paymentRepository.findBySessionId(invalidSessionId));
    }
}
