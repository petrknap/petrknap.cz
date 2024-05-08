package cz.petrknap.website.link;

import cz.petrknap.website.JpaCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(LinkController.MAPPING)
@Tag(name = "link")
public class LinkController extends JpaCrudController<Link, UUID> {
    public static final String MAPPING = "/link";

    private final LinkRepository linkRepository;

    public LinkController(LinkRepository repository) {
        this.repository = linkRepository = repository;
    }

    protected UUID doCreate(Link requested) {
        String slug = requested.getSlug();

        throwConflictIfPresent(linkRepository.findBySlug(slug));

        return linkRepository.save(new Link(slug, requested.getUrl(), requested.isForward())).getId();
    }

    protected Link doUpdate(Link actual, Link requested) {
        String slug = requested.getSlug();
        if (slug != null) {
            actual.setSlug(slug);
        }

        String url = requested.getUrl();
        if (url != null) {
            actual.setUrl(requested.getUrl());
        }

        Boolean forward = requested.getForward();
        if (forward != null) {
            actual.setForward(requested.getForward());
        }

        return linkRepository.save(actual);
    }
}
