package cz.petrknap.website;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
@RestController
public class Application {
    @Value("${website.config-json-file}")
    private String configJsonFile;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Config config() throws IOException {
        Type configType = new TypeToken<Config>(){}.getType();
        String configJson = Files.readString(Paths.get(configJsonFile));
        return new Gson().fromJson(configJson, configType);
    }

    @Operation(summary = "Healthcheck")
    @GetMapping("/ping")
    @ApiResponse(responseCode = "200", content = {@Content(mediaType = "text/plain", examples = {@ExampleObject(value = "pong")})})
    public String ping() {
        return "pong";
    }
}
