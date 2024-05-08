package cz.petrknap.website.backups;

import cz.petrknap.website.JpaCrudController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/backups")
@Tag(name = "backups")
public class BackupsController extends JpaCrudController<Metadata, String> {
    private final MetadataRepository metadataRepository;

    public BackupsController(MetadataRepository repository) {
        this.repository = metadataRepository = repository;
    }

    @Operation(summary = "Check freshness")
    @GetMapping("/{id}/freshness")
    @ApiResponse(responseCode = "204", description = "Fresh")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "500", description = "Not Fresh")
    public ResponseEntity<Void> checkFreshness(@PathVariable String id) {
        return ResponseEntity
                .status(getEntityById(id).isFresh() ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    @Operation(summary = "Refresh")
    @PutMapping("/{id}/freshness")
    @ApiResponse(responseCode = "204", description = "Refreshed")
    @ApiResponse(responseCode = "404")
    public ResponseEntity<Void> refresh(@PathVariable String id) {
        Metadata backup = getEntityById(id);
        backup.refresh();
        metadataRepository.save(backup);

        return ResponseEntity.noContent().build();
    }

    @Override
    protected String doCreate(Metadata requested) {
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
