package mate.academy.notification;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TelegramBot {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    private final RestTemplate restTemplate;
    @Value("${telegram.bot.token}")
    private String botToken;
    private int lastUpdatedId;

    public void checkTelegramUpdates() {
        String url = String.format(
                "%s%s/getUpdates?offset=%d",
                TELEGRAM_API_URL, botToken, lastUpdatedId + 1
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");

            if (updates != null && !updates.isEmpty()) {
                for (Map<String, Object> update : updates) {
                    lastUpdatedId = (int) update.get("update_id");
                    Map<String, Object> message = (Map<String, Object>) update.get("message");
                    if (message != null) {
                        String text = (String) message.get("text");
                        String chatId = String.valueOf(
                                ((Map<String, Object>) message.get("from")).get("id")
                        );
                        if ("/start".equalsIgnoreCase(text)) {
                            sendFirstMessage(chatId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFirstMessage(String chatId) {
        String message = """
                🚗 Hello! Welcome to Car Sharing Bot!

                I am here to help you keep track of your car rentals:
                - ✅ Notifications about new bookings.
                - 🚨 Reminders for overdue rentals.
                - 💳 Confirmation of successful payments.

                To get started, please select the desired command from the menu.

                If you have any questions, use the /help command.
                """;

        String url = String.format(
                "%s%s/sendMessage?chat_id=%s&text=%s",
                TELEGRAM_API_URL, botToken, chatId, message
        );

        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
}
