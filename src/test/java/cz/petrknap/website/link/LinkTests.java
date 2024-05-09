package cz.petrknap.website.link;

import cz.petrknap.website.JpaCrudControllerTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LinkTests extends JpaCrudControllerTests<Link, UUID> {
    @Autowired
    private LinkRepository repository;
    private UUID entityId;

    private static final String LINK_SLUG = "test-slug";
    private static final String LINK_LOCATION = "http://example.com";
    private static final Boolean LINK_FORWARD = false;

    @BeforeEach
    void setUp() {
        Optional<Link> optionalLink = repository.findBySlug(LINK_SLUG);
        optionalLink.ifPresent(link -> repository.delete(link));
        entityId = repository.save(new Link(LINK_SLUG, LINK_LOCATION, LINK_FORWARD)).getId();
    }

    @Test
    void processesSlug() throws Exception {
        mvc.perform(get(LinkToController.MAPPING + "/" + LINK_SLUG))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(LINK_LOCATION)))
        ;
    }

    @Override
    protected LinkRepository getRepository() {
        return repository;
    }

    @Override
    protected String getRequestMapping() {
        return LinkController.MAPPING;
    }

    @Override
    protected UUID getEntityId() {
        return entityId;
    }

    @Override
    protected Map<String, String> getCreateBodyAsKeyToRawValue() {
        return new HashMap<>() {{
            put("slug", "\"" + LINK_SLUG + "\"");
            put("location", "\"" + LINK_LOCATION + "\"");
            put("forward", LINK_FORWARD.toString());
        }};
    }

    @Override
    protected Map<String, String> getUpdateBodyAsKeyToRawValue() {
        return new HashMap<>() {{
            put("slug", "\"" + LINK_SLUG + "/a\"");
            put("location", "\"" + LINK_LOCATION + "/a\"");
        }};
    }
}
