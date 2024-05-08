package cz.petrknap.website.backups;

import cz.petrknap.website.JpaCrudController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping(BackupsController.MAPPING)
@Tag(name = "backups")
public class BackupsController extends JpaCrudController<Metadata, UUID> {
    public static final String MAPPING = "/backups";

    private final MetadataRepository metadataRepository;

    public BackupsController(MetadataRepository repository) {
        this.repository = metadataRepository = repository;
    }

    @Operation(summary = "Check freshness")
    @GetMapping("/{identifier}/freshness")
    @ApiResponse(responseCode = "204", description = "Fresh")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "500", description = "Not Fresh")
    public ResponseEntity<Void> checkFreshness(@PathVariable String identifier) {
        Metadata backup = metadataRepository.findByIdentifier(identifier).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity
                .status(backup.isFresh() ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    @Operation(summary = "Refresh")
    @PutMapping("/{identifier}/freshness")
    @ApiResponse(responseCode = "204", description = "Refreshed")
    @ApiResponse(responseCode = "404")
    public ResponseEntity<Void> refresh(@PathVariable String identifier) {
        Metadata backup = metadataRepository.findByIdentifier(identifier).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        backup.refresh();
        metadataRepository.save(backup);

        return ResponseEntity.noContent().build();
    }

    @Override
    protected UUID doCreate(Metadata requested) {
        String identifier = requested.getIdentifier();

        throwConflictIfPresent(metadataRepository.findByIdentifier(identifier));

        return metadataRepository.save(new Metadata(identifier, requested.getFreshForHours())).getId();
    }

    @Override
    protected Metadata doUpdate(Metadata actual, Metadata requested) {
        Integer freshForHours = requested.getFreshForHours();
        if (freshForHours != null) {
            actual.setFreshForHours(freshForHours);
        }

        return metadataRepository.save(actual);
    }
}
