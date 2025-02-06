package mate.academy.service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.payment.PaymentDtoOverview;
import mate.academy.dto.payment.PaymentRequestDto;
import mate.academy.dto.payment.PaymentResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.PaymentMapper;
import mate.academy.model.Payment;
import mate.academy.model.Rental;
import mate.academy.notification.NotificationService;
import mate.academy.repository.PaymentRepository;
import mate.academy.repository.RentalRepository;
import mate.academy.service.stripe.StripeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    public static final BigDecimal FINE_MULTIPLIER = BigDecimal.valueOf(1.5);
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final StripeService stripeService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public List<PaymentDtoOverview> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByRentalUserId(userId).stream()
                .map(paymentMapper::toDtoOverview)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponseDto createPaymentSession(
            PaymentRequestDto requestDto,
            UriComponentsBuilder uriComponentsBuilder
    ) throws StripeException {
        Rental rental = rentalRepository.findById(requestDto.getRentalId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental with id: "
                        + requestDto.getRentalId())
        );

        BigDecimal amount = calculateAmount(rental, requestDto.getPaymentType());

        Session session = stripeService.createSession(
                rental, requestDto.getPaymentType(), amount, uriComponentsBuilder
        );

        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(Payment.Type.valueOf(requestDto.getPaymentType()));
        payment.setRental(rental);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        payment.setAmount(amount);

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public void successfulPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with session id: "
                        + sessionId)
        );

        payment.setStatus(Payment.Status.PAID);

        notificationService.sendSuccessPaymentNotification(payment);

        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public String cancelPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment with session id: "
                        + sessionId)
        );

        if (payment.getStatus().equals(Payment.Status.PENDING)) {
            notificationService.sendCancelPaymentNotification(payment);
        }

        return "Payment canceled. You can try again within 24 hours.";
    }

    private BigDecimal calculateAmount(Rental rental, String type) {
        int overdueDays = rental.getActualReturnDate().getDayOfYear() - rental
                .getReturnDate().getDayOfYear();
        int daysOfSharing = rental.getReturnDate().getDayOfYear() - rental
                .getRentalDate().getDayOfYear();

        if ("FINE".equalsIgnoreCase(type)) {
            return rental.getCar().getDailyFee()
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(FINE_MULTIPLIER);
        }
        return rental.getCar().getDailyFee().multiply(BigDecimal.valueOf(daysOfSharing));
    }

}
