package mate.academy.dto.payment;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentDtoOverview {
    private String status;
    private String type;
    private Long rentalId;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amount;
}
