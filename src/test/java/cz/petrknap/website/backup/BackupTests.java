package cz.petrknap.website.backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BackupTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MetadataRepository repository;

    private static final String BACKUP_IDENTIFIER = "test-backup";
    private static final Integer BACKUP_FRESH_FOR_HOURS = 123;

    @BeforeEach
    public void setUp() {
        repository.deleteById(BACKUP_IDENTIFIER);
        repository.save(new Metadata(BACKUP_IDENTIFIER, BACKUP_FRESH_FOR_HOURS));
    }

    @Test
    public void listsMetadata() throws Exception {
        mvc.perform(get("/backup/"))
                .andExpect(status().isOk())
        ;
    }

    @Test
    public void createsMetadata() throws Exception {
        repository.deleteById(BACKUP_IDENTIFIER);

        mvc.perform(post("/backup/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + BACKUP_IDENTIFIER + "\",\"freshForHours\":" + BACKUP_FRESH_FOR_HOURS + "}")
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(BACKUP_IDENTIFIER)))
        ;
    }

    @Test
    public void showsMetadata() throws Exception {
        mvc.perform(get("/backup/" + BACKUP_IDENTIFIER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"identifier\":\"" + BACKUP_IDENTIFIER + "\"")))
                .andExpect(content().string(containsString("\"freshForHours\":" + BACKUP_FRESH_FOR_HOURS)))
        ;
    }

    @Test
    public void updatesMetadata() throws Exception {
        mvc.perform(put("/backup/" + BACKUP_IDENTIFIER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"freshForHours\":" + (BACKUP_FRESH_FOR_HOURS + 1) + "}")
                )
                .andExpect(status().isNoContent())
        ;

        assertThat(repository.findByIdentifier(BACKUP_IDENTIFIER).orElseThrow().getFreshForHours()).isEqualTo(BACKUP_FRESH_FOR_HOURS + 1);
    }

    @Test
    public void deletesMetadata() throws Exception {
        mvc.perform(delete("/backup/" + BACKUP_IDENTIFIER))
                .andExpect(status().isNoContent())
        ;

        assertThat(repository.findById(BACKUP_IDENTIFIER)).isEmpty();
    }

    @Test
    public void checksMetadataFreshness() throws Exception {
        mvc.perform(get("/backup/" + BACKUP_IDENTIFIER + "/freshness"))
                .andExpect(status().isInternalServerError())
        ;

        Metadata backup = repository.findById(BACKUP_IDENTIFIER).orElseThrow();
        backup.refresh();
        repository.save(backup);

        mvc.perform(get("/backup/" + BACKUP_IDENTIFIER + "/freshness"))
                .andExpect(status().isNoContent())
        ;
    }

    @Test
    public void refreshesMetadata() throws Exception {
        mvc.perform(put("/backup/" + BACKUP_IDENTIFIER + "/freshness"))
                .andExpect(status().isNoContent())
        ;

        assertThat(repository.findByIdentifier(BACKUP_IDENTIFIER).orElseThrow().isFresh()).isTrue();
    }
}
