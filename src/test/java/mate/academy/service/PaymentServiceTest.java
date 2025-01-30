package mate.academy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mate.academy.dto.payment.PaymentDtoOverview;
import mate.academy.dto.payment.PaymentRequestDto;
import mate.academy.dto.payment.PaymentResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.PaymentMapper;
import mate.academy.model.Car;
import mate.academy.model.Payment;
import mate.academy.model.Rental;
import mate.academy.notification.NotificationService;
import mate.academy.repository.PaymentRepository;
import mate.academy.repository.RentalRepository;
import mate.academy.service.payment.PaymentServiceImpl;
import mate.academy.service.stripe.StripeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("Get payments when user has")
    void getPaymentsByUserId_UserHasPayments_ShouldReturnPaymentDtoList() {
        Long userId = 1L;

        Payment payment1 = new Payment();
        payment1.setId(101L);
        Payment payment2 = new Payment();
        payment2.setId(102L);

        PaymentDtoOverview dto1 = new PaymentDtoOverview();
        PaymentDtoOverview dto2 = new PaymentDtoOverview();

        List<Payment> payments = List.of(payment1, payment2);

        when(paymentRepository.findByRentalUserId(userId)).thenReturn(payments);
        when(paymentMapper.toDtoOverview(payment1)).thenReturn(dto1);
        when(paymentMapper.toDtoOverview(payment2)).thenReturn(dto2);

        List<PaymentDtoOverview> expectedDtos = List.of(dto1, dto2);

        List<PaymentDtoOverview> actualDtos = paymentService.getPaymentsByUserId(userId);

        assertNotNull(actualDtos);
        assertEquals(expectedDtos.size(), actualDtos.size());
        assertEquals(expectedDtos, actualDtos);

        verify(paymentRepository, times(1)).findByRentalUserId(userId);
        verify(paymentMapper, times(2)).toDtoOverview(any(Payment.class));
    }

    @Test
    @DisplayName("Get payments when user hasn't")
    void getPaymentsByUserId_UserHasNoPayments_ShouldReturnEmptyList() {
        Long userId = 2L;
        when(paymentRepository.findByRentalUserId(userId)).thenReturn(Collections.emptyList());

        List<PaymentDtoOverview> actualDtos = paymentService.getPaymentsByUserId(userId);

        assertNotNull(actualDtos);
        assertTrue(actualDtos.isEmpty());

        verify(paymentRepository, times(1)).findByRentalUserId(userId);
        verify(paymentMapper, never()).toDtoOverview(any(Payment.class));
    }

    @Test
    @DisplayName("Create payment session with valid data")
    void createPaymentSession_ValidData_ShouldReturnPaymentResponseDto() throws StripeException {
        Long rentalId = 1L;
        String paymentType = "PAYMENT";

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setRentalId(rentalId);
        requestDto.setPaymentType(paymentType);

        Car car = new Car();
        car.setInventory(12);
        car.setDailyFee(BigDecimal.valueOf(120.99));

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setRentalDate(LocalDateTime.now());
        rental.setReturnDate(LocalDateTime.now().plusDays(12));
        rental.setActualReturnDate(LocalDateTime.now().plusDays(11));
        rental.setCar(car);

        String sessionId = "sess_123";
        String sessionUrl = "https://checkout.stripe.com/pay/sess_123";

        Session session = mock(Session.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.getUrl()).thenReturn(sessionUrl);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(stripeService.createSession(any(Rental.class), eq(paymentType),
                any(BigDecimal.class), any(UriComponentsBuilder.class))).thenReturn(session);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            savedPayment.setId(100L);
            return savedPayment;
        });

        PaymentResponseDto expectedResponse = new PaymentResponseDto();
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expectedResponse);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost");

        PaymentResponseDto actualResponse = paymentService
                .createPaymentSession(requestDto, uriBuilder);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        BigDecimal expectedAmount = BigDecimal.valueOf(1451.88);
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(stripeService, times(1)).createSession(rental, paymentType,
                expectedAmount, uriBuilder);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentMapper, times(1)).toDto(any(Payment.class));
    }

    @Test
    @DisplayName("Successful payment with a valid session id")
    public void successfulPayment_ValidSessionId_ShouldUpdatePaymentStatus() {
        String sessionId = "session123";
        Payment payment = new Payment();
        payment.setSessionId(sessionId);
        payment.setStatus(Payment.Status.PENDING);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        doNothing().when(notificationService).sendSuccessPaymentNotification(payment);

        paymentService.successfulPayment(sessionId);

        assertEquals(Payment.Status.PAID, payment.getStatus());
        verify(paymentRepository, times(1)).save(payment);
        verify(notificationService, times(1)).sendSuccessPaymentNotification(payment);
    }

    @Test
    @DisplayName("Successful paymenr when payment not found")
    public void successfulPayment_PaymentNotFound_ShouldThrowException() {
        String sessionId = "nonExistentSession";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.successfulPayment(sessionId));

        verify(paymentRepository, times(1)).findBySessionId(sessionId);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("Cancel payment when payment has status Pending")
    public void cancelPayment_PaymentPending_ShouldSendCancelNotification() {
        String sessionId = "validSession";
        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PENDING);
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        String result = paymentService.cancelPayment(sessionId);

        assertEquals("Payment canceled. You can try again within 24 hours.", result);

        verify(paymentRepository, times(1)).findBySessionId(sessionId);

        verify(notificationService, times(1)).sendCancelPaymentNotification(payment);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Cancel payment when payment not found")
    public void cancelPayment_PaymentNotFound_ShouldThrowException() {
        String sessionId = "nonExistentSession";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> paymentService.cancelPayment(sessionId));

        verify(paymentRepository, times(1)).findBySessionId(sessionId);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(notificationService);
    }
}
