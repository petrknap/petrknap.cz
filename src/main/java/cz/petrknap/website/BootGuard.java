package cz.petrknap.website;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class BootGuard {
    @Value("${spring.datasource.url}")
    private String springDatasourceUrl;

    private final Config.BootGuard config;
    private final Logger logger = LoggerFactory.getLogger(BootGuard.class);

    public BootGuard(Config config) {
        this.config = config.bootGuard();
    }

    @PostConstruct
    public void copySqlite() {
        try {
            String sqliteFile = springDatasourceUrl.replace("jdbc:sqlite:", "");
            Files.copy(Paths.get(sqliteFile), Paths.get(config.sqliteCopyFile()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException reason) {
            logger.warn("Could not copy SQLite", reason);
        }
    }
}
