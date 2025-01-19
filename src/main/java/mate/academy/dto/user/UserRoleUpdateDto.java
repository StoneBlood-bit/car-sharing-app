package mate.academy.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRoleUpdateDto {
    @NotBlank
    private String role;
}
