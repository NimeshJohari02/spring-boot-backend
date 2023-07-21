package dev.nimesh.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileUploadController {

    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "HealthCheck Up and Running ";
    }
}
