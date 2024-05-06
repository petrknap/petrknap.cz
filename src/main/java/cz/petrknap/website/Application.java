package cz.petrknap.website;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Operation(summary = "Healthcheck")
    @GetMapping("/ping")
    @ApiResponse(responseCode = "200", content = {@Content(mediaType = "text/plain", examples = {@ExampleObject(value = "pong")})})
    public String ping() {
        return "pong";
    }
}
