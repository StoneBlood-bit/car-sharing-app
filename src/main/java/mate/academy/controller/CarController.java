package mate.academy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.car.CarDto;
import mate.academy.service.car.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Car Management",
        description = "Endpoints for managing the car catalog, "
                + "including adding, retrieving, updating, and deleting car entries."
)
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Create a new car",
            description = "Allows a manager to add a new car to the catalog."
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CarDto createCar(@RequestBody @Valid CarDto carDto) {
        return carService.save(carDto);
    }

    @Operation(
            summary = "Retrieve all cars",
            description = "Returns a paginated list of all cars available in the catalog."
    )
    @GetMapping
    public Page<CarDto> getAll(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @Operation(
            summary = "Retrieve car by ID",
            description = "Fetches detailed information about"
                    + " a specific car using its unique identifier."
    )
    @GetMapping("/{id}")
    public CarDto getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update car details",
            description = "Allows a manager to replace the details of an existing car"
                    + " with new data. Requires MANAGER role."
    )
    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable Long id, @RequestBody @Valid CarDto carDto) {
        return carService.update(carDto, id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Delete a car",
            description = "Allows a manager to remove a car from the catalog"
                    + " using its unique identifier. Requires MANAGER role."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
