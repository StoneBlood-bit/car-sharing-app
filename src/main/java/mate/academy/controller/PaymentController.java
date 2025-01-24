package mate.academy.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.payment.PaymentDtoOverview;
import mate.academy.dto.payment.PaymentRequestDto;
import mate.academy.dto.payment.PaymentResponseDto;
import mate.academy.service.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Payment manager", description = "Endpoint for maneging payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get a payment by id", description = "Find a payment by user`s id")
    @GetMapping
    public List<PaymentDtoOverview> getPayments(@RequestParam("user_id") Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create payment session", description = "Create payment session")
    @PostMapping
    public PaymentResponseDto createPaymentSession(
            @RequestBody PaymentRequestDto requestDto,
            UriComponentsBuilder uriComponentsBuilder
    ) throws StripeException {
        return paymentService.createPaymentSession(requestDto, uriComponentsBuilder);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Process successful payment", description = "Mark payment successful")
    @GetMapping("/success")
    public ResponseEntity<String> success(@RequestParam("session_id") String sessionId) {
        paymentService.successfulPayment(sessionId);
        return ResponseEntity.ok("Payment successfully completed!");
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Processing a payment reversal",
            description = "Processing a payment reversal"
    )
    @GetMapping("/cancel")
    public ResponseEntity<String> cancel() {
        return ResponseEntity.ok(paymentService.cancelPayment());
    }
}
