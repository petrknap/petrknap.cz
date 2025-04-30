package cz.petrknap.website.www;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(WwwController.MAPPING)
public class WwwController {
    public static final String MAPPING = "/www";

    private final Logger logger = LoggerFactory.getLogger(WwwController.class);

    private String cachedIndex;

    @GetMapping("/")
    public ModelAndView index() {
        if (cachedIndex == null) {
            try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(new URL("https://petrknap.github.io/index_cz.html").openStream(), StandardCharsets.UTF_8))) {
                cachedIndex = inputStream.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
                throw new ErrorResponseException(HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
        return new ModelAndView("www/index", Map.of("content", cachedIndex));
    }
}
