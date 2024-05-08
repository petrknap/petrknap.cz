package cz.petrknap.website.link;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(LinkToController.MAPPING)
public class LinkToController {
    public static final String MAPPING = LinkController.MAPPING + "/to";

    private final LinkRepository linkRepository;

    public LinkToController(LinkRepository repository) {
        linkRepository = repository;
    }

    @GetMapping("/**")
    public String processSlug(HttpServletRequest request) {
        String slug = request.getRequestURL().toString().split(MAPPING + "/")[1];
        Link link = linkRepository.findBySlug(slug).orElseThrow(() -> new ErrorResponseException(HttpStatus.NOT_FOUND));

        if (link.isForward()) {
            return "forward:" + MAPPING + "-forwardable/" + link.getUrl();
        } else {
            return "redirect:" + link.getUrl();
        }
    }
}
