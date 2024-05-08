package cz.petrknap.website.backups;

import cz.petrknap.website.JpaCrudControllerTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackupsTests extends JpaCrudControllerTests<Metadata, UUID> {
    @Autowired
    private MetadataRepository repository;
    private UUID entityId;

    private static final String BACKUP_IDENTIFIER = "test-backup";
    private static final Integer BACKUP_FRESH_FOR_HOURS = 123;

    @BeforeEach
    void setUp() {
        Optional<Metadata> optionalMetadata = repository.findByIdentifier(BACKUP_IDENTIFIER);
        optionalMetadata.ifPresent(metadata -> repository.delete(metadata));
        entityId = repository.save(new Metadata(BACKUP_IDENTIFIER, BACKUP_FRESH_FOR_HOURS)).getId();
    }

    @Test
    void checksFreshness() throws Exception {
        mvc.perform(get(BackupsController.MAPPING + "/" + BACKUP_IDENTIFIER + "/freshness"))
                .andExpect(status().isInternalServerError())
        ;

        Metadata backup = repository.findById(entityId).orElseThrow();
        backup.refresh();
        repository.save(backup);

        mvc.perform(get(BackupsController.MAPPING + "/" + BACKUP_IDENTIFIER + "/freshness"))
                .andExpect(status().isNoContent())
        ;
    }

    @Test
    void refreshes() throws Exception {
        mvc.perform(put(BackupsController.MAPPING + "/" + BACKUP_IDENTIFIER + "/freshness"))
                .andExpect(status().isNoContent())
        ;

        assertThat(repository.findByIdentifier(BACKUP_IDENTIFIER).orElseThrow().isFresh()).isTrue();
    }

    @Override
    protected MetadataRepository getRepository() {
        return repository;
    }

    @Override
    protected String getRequestMapping() {
        return BackupsController.MAPPING;
    }

    @Override
    protected UUID getEntityId() {
        return entityId;
    }

    @Override
    protected Map<String, String> getCreateBodyAsKeyToRawValue() {
        return new HashMap<>() {{
            put("identifier", "\"" + BACKUP_IDENTIFIER + "\"");
            put("freshForHours", BACKUP_FRESH_FOR_HOURS.toString());
        }};
    }

    @Override
    protected Map<String, String> getUpdateBodyAsKeyToRawValue() {
        return new HashMap<>() {{
            put("freshForHours", BACKUP_FRESH_FOR_HOURS + "1");
        }};
    }
}
