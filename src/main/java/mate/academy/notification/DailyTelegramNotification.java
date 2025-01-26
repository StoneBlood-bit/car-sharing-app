package mate.academy.notification;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.model.Rental;
import mate.academy.repository.RentalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyTelegramNotification {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 10 * * *")
    public void sendDailyNotification() {
        List<Rental> nonOverdueRentals = rentalRepository
                .findByReturnDateAfterOrActualReturnDateIsNotNull(LocalDateTime.now());

        if (!nonOverdueRentals.isEmpty()) {
            for (Rental rental : nonOverdueRentals) {
                notificationService.sendNoOverdueRentalNotification(rental);
            }
        }

        List<Rental> overdueRentals = rentalRepository
                .findByReturnDateBeforeAndActualReturnDateIsNull(LocalDateTime.now());

        if (!overdueRentals.isEmpty()) {
            for (Rental rental : overdueRentals) {
                notificationService.sendNoOverdueRentalNotification(rental);
            }
        }
    }

}
