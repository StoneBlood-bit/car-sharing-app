package mate.academy.dto.rental;

import lombok.Data;

@Data
public class RentalFilterRequestDto {
    private Long userId;
    private Boolean isActive;
}
