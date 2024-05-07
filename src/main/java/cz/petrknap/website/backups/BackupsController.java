package cz.petrknap.website.backups;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/backups")
public class BackupsController {
    private final MetadataRepository repository;

    public BackupsController(MetadataRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "List metadata")
    @GetMapping("/")
    public List<Metadata> list() {
        return repository.findAll();
    }

    @Operation(summary = "Create a metadata")
    @PostMapping("/")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {@Content(examples = {@ExampleObject(value = "{\"identifier\": \"my-backup\", \"freshForHours\": 28}")})})
    @ApiResponse(responseCode = "201", headers = {@Header(name = HttpHeaders.LOCATION, description = "Relative link to the created metadata")})
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "409")
    public ResponseEntity<Void> create(@RequestBody Metadata requested) {
        String identifier = requested.getIdentifier();
        Integer freshForHours = requested.getFreshForHours();

        if (identifier == null || freshForHours == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Optional<Metadata> backup = repository.findByIdentifier(identifier);
        if (backup.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        repository.save(new Metadata(identifier, freshForHours));

        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.LOCATION, identifier).body(null);
    }

    @Operation(summary = "Show the metadata")
    @GetMapping("/{identifier}")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = {@Content})
    public Metadata show(@PathVariable("identifier") Metadata backup) {
        return backup;
    }

    @Operation(summary = "Update the metadata")
    @PutMapping("/{identifier}")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {@Content(examples = {@ExampleObject(value = "{\"freshForHours\": 28}")})})
    @ApiResponse(responseCode = "204", description = "Updated")
    @ApiResponse(responseCode = "400")
    @ApiResponse(responseCode = "404")
    public ResponseEntity<Void> update(@PathVariable("identifier") Metadata backup, @RequestBody Metadata requested) {
        backup.setFreshForHours(requested.getFreshForHours());
        repository.save(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Delete the metadata")
    @DeleteMapping("/{identifier}")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404")
    public ResponseEntity<Void> delete(@PathVariable("identifier") Metadata backup) {
        repository.delete(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Check the metadata freshness")
    @GetMapping("/{identifier}/freshness")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponse(responseCode = "204", description = "Fresh")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "500", description = "Not Fresh")
    public ResponseEntity<Void> checkFreshness(@PathVariable("identifier") Metadata backup) {
        return ResponseEntity.status(backup.isFresh() ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Operation(summary = "Refresh the metadata")
    @PutMapping("/{identifier}/freshness")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponse(responseCode = "204", description = "Refreshed")
    @ApiResponse(responseCode = "404")
    public ResponseEntity<Void> refresh(@PathVariable("identifier") Metadata backup) {
        backup.refresh();
        repository.save(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
