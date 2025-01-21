package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.rental.RentalDetailDto;
import mate.academy.dto.rental.RentalFilterRequestDto;
import mate.academy.dto.rental.RentalRequestDto;
import mate.academy.model.User;
import mate.academy.service.rental.RentalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental manager", description = "Endpoint of maneging rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private Logger logger = LoggerFactory.getLogger(RentalController.class);
    private final RentalService rentalService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new rental", description = "Create a new rental")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RentalDetailDto createRental(
            @RequestBody @Valid RentalRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return rentalService.createRental(requestDto, user.getEmail());
    }

    @PreAuthorize("hasRole('MANAGER') or (#userId == null && principal.id == #currentUser.id)")
    @Operation(summary = "Get all rentals", description = "Get list of all rentals")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<RentalDetailDto> getRentals(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal User currentUser
    ) {
        logger.info("Request received by user: {}, role: {}, userId: {}, isActive: {}",
                currentUser.getEmail(), currentUser.getRole(), userId, isActive);

        RentalFilterRequestDto filter = new RentalFilterRequestDto();
        filter.setUserId(userId);
        filter.setIsActive(isActive);

        List<RentalDetailDto> rentals = rentalService.getRentals(filter, currentUser);

        logger.info("Returning {} rentals", rentals.size());

        return rentals;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Complete a rental",
            description = "Mark a rental as completed and increase car inventory."
    )
    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> completeRental(@PathVariable Long id) {
        rentalService.completeRental(id);
        return ResponseEntity.ok("Rental completed successfully.");
    }
}
