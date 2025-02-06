package mate.academy.notification;

import mate.academy.model.Payment;
import mate.academy.model.Rental;

public interface NotificationService {
    void sendNotification(Long userId, String message);

    void sendNewRentalNotification(Rental rental);

    void sendOverdueRentalNotification(Rental rental);

    void sendNoOverdueRentalNotification(Rental rental);

    void sendReturnRentalNotification(Rental rental);

    void sendSuccessPaymentNotification(Payment payment);

    void sendCancelPaymentNotification(Payment payment);
}
