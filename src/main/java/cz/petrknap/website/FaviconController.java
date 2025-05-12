package cz.petrknap.website;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FaviconController {
    @GetMapping("/{name}/favicon.ico")
    public String forward(@PathVariable String name) {
        return "forward:/favicon/" + name + ".ico";
    }
}
