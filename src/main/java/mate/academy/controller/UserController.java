package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.user.UpdateUserProfileDto;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.dto.user.UserRoleUpdateDto;
import mate.academy.model.User;
import mate.academy.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "User Management",
        description = "Endpoints for managing user accounts, including profile retrieval,"
                + " role updates, and profile modifications."
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Retrieve user's profile",
            description = "Allows a customer to view their own profile information."
                    + " Requires CUSTOMER role."
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(@AuthenticationPrincipal User user) {
        UserResponseDto responseDto = userService.getUserById(user.getId());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update user's role",
            description = "Allows a manager to change the role of "
                    + "a specific user by their unique identifier. Requires MANAGER role."
    )
    @PutMapping("/{id}/role")
    public UserResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateDto updateDto
    ) {
        return userService.updateRole(updateDto, id);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Update user's profile",
            description = "Allows a customer to update their profile details. "
                    + "Requires CUSTOMER role."
    )
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateUserProfileDto updateUserProfileDto
    ) {
        userService.updateUserProfile(user.getId(), updateUserProfileDto);
        return ResponseEntity.noContent().build();
    }
}
