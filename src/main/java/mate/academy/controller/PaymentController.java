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

@Tag(
        name = "Payment Management",
        description = """
        Endpoints for managing payment processes, including session creation, 
        retrieval of payments, successful payment processing, and payment reversals.
        """
)
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Retrieve payments by user ID",
            description = """
            Fetch a list of payments associated with a specific user's ID. 
            This endpoint is restricted to users with the MANAGER role.
            """
    )
    @GetMapping
    public List<PaymentDtoOverview> getPayments(@RequestParam("user_id") Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Create a new payment session",
            description = """
            Initiates a new payment session for a customer. The request body 
            must include payment details. Returns a session ID for payment processing.
            """
    )
    @PostMapping
    public PaymentResponseDto createPaymentSession(
            @RequestBody PaymentRequestDto requestDto,
            UriComponentsBuilder uriComponentsBuilder
    ) throws StripeException {
        return paymentService.createPaymentSession(requestDto, uriComponentsBuilder);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Process a successful payment",
            description = """
            Marks a payment as successfully completed using the provided session ID. 
            This endpoint is restricted to users with the MANAGER role.
            """
    )
    @GetMapping("/success")
    public ResponseEntity<String> success(@RequestParam("session_id") String sessionId) {
        paymentService.successfulPayment(sessionId);
        return ResponseEntity.ok("Payment successfully completed!");
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Cancel a payment session",
            description = """
            Processes a payment reversal for a specific session ID. 
            This endpoint allows customers to cancel their payment sessions.
            """
    )
    @GetMapping("/cancel")
    public ResponseEntity<String> cancel(@RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok(paymentService.cancelPayment(sessionId));
    }
}
