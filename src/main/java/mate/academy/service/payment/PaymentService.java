package mate.academy.service.payment;

import com.stripe.exception.StripeException;
import java.util.List;
import mate.academy.dto.payment.PaymentDtoOverview;
import mate.academy.dto.payment.PaymentRequestDto;
import mate.academy.dto.payment.PaymentResponseDto;
import org.springframework.web.util.UriComponentsBuilder;

public interface PaymentService {
    List<PaymentDtoOverview> getPaymentsByUserId(Long userId);

    PaymentResponseDto createPaymentSession(
            PaymentRequestDto requestDto,
            UriComponentsBuilder uriComponentsBuilder
    ) throws StripeException;

    void successfulPayment(String sessionId);

    String cancelPayment();
}
