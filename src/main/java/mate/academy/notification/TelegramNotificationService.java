package mate.academy.notification;

import lombok.RequiredArgsConstructor;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.exception.NotificationException;
import mate.academy.model.Payment;
import mate.academy.model.Rental;
import mate.academy.model.User;
import mate.academy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public void sendNotification(Long userId, String message) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with id: " + userId)
        );
        String chatId = user.getChatId();

        if (chatId == null) {
            throw new NotificationException("User with id: " + userId + " don't have chat id");
        }

        String url = String.format(
                "%s%s/sendMessage?chat_id=%s&text=%s",
                TELEGRAM_API_URL, botToken, chatId, message
        );

        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            throw new NotificationException("Failed to send notification: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendNewRentalNotification(Rental rental) {
        String message = String.format(
                "Hello, You Created New Rental🚗\n\n"
                        + "User: %s\nCar: %s %s\nRental Date: %s\nReturn Date: %s\n"
                        + "Thank you for choosing our service, best of luck!",
                rental.getUser().getFirstName(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getRentalDate(),
                rental.getReturnDate()
        );
        sendNotification(rental.getUser().getId(), message);
    }

    @Override
    public void sendOverdueRentalNotification(Rental rental) {
        String message = String.format(
                "Overdue Rental Alert!🚨\n\n"
                        + "User: %s\nCar: %s %s\nRental Date: %s\nReturn Date: %s\n",
                rental.getUser().getFirstName(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getRentalDate(),
                rental.getReturnDate()
        );

        sendNotification(rental.getUser().getId(), message);
    }

    @Override
    public void sendNoOverdueRentalNotification(Rental rental) {
        String message = "No rentals overdue today, thank you!";

        sendNotification(rental.getUser().getId(), message);
    }

    @Async
    @Override
    public void sendReturnRentalNotification(Rental rental) {
        String message = String.format(
                "Rental Returned!\n\n"
                        + "User: %s\nCar: %s %s\nRental Date: %s\nReturn Date: %s\n",
                rental.getUser().getFirstName(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getRentalDate(),
                rental.getReturnDate()
        );

        sendNotification(rental.getUser().getId(), message);
    }

    @Async
    @Override
    public void sendSuccessPaymentNotification(Payment payment) {
        String message = "Payment was successful!";

        sendNotification(payment.getRental().getUser().getId(), message);
    }

    @Async
    @Override
    public void sendCancelPaymentNotification(Payment payment) {
        String message = "Payment has been cancelled!";

        sendNotification(payment.getRental().getUser().getId(), message);
    }
}
