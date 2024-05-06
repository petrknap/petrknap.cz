package cz.petrknap.website.backup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/backup")
public class BackupController {
    @Autowired
    private MetadataRepository repository;

    @Operation(summary = "List metadata")
    @GetMapping("/")
    public List<Metadata> list() {
        return repository.findAll();
    }

    @Operation(summary = "Create a metadata")
    @PostMapping("/")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {@Content(examples = {@ExampleObject(value = "{\"identifier\": \"my-backup\", \"freshForHours\": 28}")})})
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = {@Content}),
            @ApiResponse(responseCode = "409", content = {@Content}),
            @ApiResponse(responseCode = "201", content = {@Content}, headers = {@Header(name = HttpHeaders.LOCATION, description = "Relative link to the created metadata")}),
    })
    public ResponseEntity<?> create(@RequestBody Metadata requested) {
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
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = {@Content}),
            @ApiResponse(responseCode = "200"),
    })
    public Metadata show(@PathVariable("identifier") Metadata backup) {
        return backup;
    }

    @Operation(summary = "Update the metadata")
    @PutMapping("/{identifier}")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {@Content(examples = {@ExampleObject(value = "{\"freshForHours\": 28}")})})
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = {@Content}),
            @ApiResponse(responseCode = "404", content = {@Content}),
            @ApiResponse(responseCode = "204", content = {@Content}, description = "Updated"),
    })
    public ResponseEntity<?> update(@PathVariable("identifier") Metadata backup, @RequestBody Metadata requested) {
        backup.setFreshForHours(requested.getFreshForHours());
        repository.save(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Delete the metadata")
    @DeleteMapping("/{identifier}")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = {@Content()}),
            @ApiResponse(responseCode = "204", content = {@Content()}, description = "Deleted"),
    })
    public ResponseEntity<?> delete(@PathVariable("identifier") Metadata backup) {
        repository.delete(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Check the metadata freshness")
    @GetMapping("/{identifier}/freshness")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = {@Content()}),
            @ApiResponse(responseCode = "500", content = {@Content()}, description = "Not Fresh"),
            @ApiResponse(responseCode = "204", content = {@Content()}, description = "Fresh"),
    })
    public ResponseEntity<?> checkFreshness(@PathVariable("identifier") Metadata backup) {
        return ResponseEntity.status(backup.isFresh() ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Operation(summary = "Refresh the metadata")
    @PutMapping("/{identifier}/freshness")
    @Parameter(name = "identifier", content = {@Content(mediaType = "plain/text")}, example = "my-backup")
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = {@Content()}),
            @ApiResponse(responseCode = "204", content = {@Content()}, description = "Refreshed"),
    })
    public ResponseEntity<?> refresh(@PathVariable("identifier") Metadata backup) {
        backup.refresh();
        repository.save(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
