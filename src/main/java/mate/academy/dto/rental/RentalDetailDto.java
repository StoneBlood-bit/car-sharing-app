package mate.academy.dto.rental;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RentalDetailDto {
    private Long id;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private LocalDateTime actualReturnDate;
    private String carModel;
    private String userEmail;
}
