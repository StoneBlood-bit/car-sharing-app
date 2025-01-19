package mate.academy.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Endpoint to check the status app")
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public String checkHealth() {
        return "Application is running!";
    }
}
