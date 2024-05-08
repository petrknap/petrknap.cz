package cz.petrknap.website.link;

import cz.petrknap.website.JpaCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/link")
@Tag(name = "link")
public class LinkController extends JpaCrudController<Link, Long> {
    private final LinkRepository linkRepository;

    public LinkController(LinkRepository repository) {
        this.repository = linkRepository = repository;
    }

    protected Long doCreate(Link requested) {
        String slug = requested.getSlug();

        throwConflictIfPresent(linkRepository.findBySlug(slug));

        return linkRepository.save(new Link(slug, requested.getUrl())).getId();
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

        return linkRepository.save(actual);
    }
}
