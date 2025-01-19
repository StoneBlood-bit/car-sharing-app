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

@Tag(name = "Car manager", description = "Endpoint for maneging cars")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create a new car", description = "Create a new car")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CarDto createCar(@RequestBody @Valid CarDto carDto) {
        return carService.save(carDto);
    }

    @Operation(summary = "Get all cars", description = "Get list of all available cars")
    @GetMapping
    public Page<CarDto> getAll(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Get car by id",
            description = "Find a car with a passed id"
    )
    @GetMapping("/{id}")
    public CarDto getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Update a car",
            description = "Replace the existing car with a new one"
    )
    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable Long id, @RequestBody @Valid CarDto carDto) {
        return carService.update(carDto, id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Delete category",
            description = "Delete a category with a passed id"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
