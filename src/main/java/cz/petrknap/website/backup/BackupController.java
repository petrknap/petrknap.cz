package cz.petrknap.website.backup;

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

    @GetMapping("/")
    public List<Metadata> list() {
        return repository.findAll();
    }

    @PostMapping("/")
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

    @GetMapping("/{identifier}")
    public Metadata show(@PathVariable("identifier") Metadata backup) {
        return backup;
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<?> update(@PathVariable("identifier") Metadata backup, @RequestBody Metadata requested) {
        backup.setFreshForHours(requested.getFreshForHours());
        repository.save(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<?> delete(@PathVariable("identifier") Metadata backup) {
        repository.delete(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @GetMapping("/{identifier}/freshness")
    public ResponseEntity<?> checkFreshness(@PathVariable("identifier") Metadata backup) {
        return ResponseEntity.status(backup.isFresh() ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @PutMapping("/{identifier}/freshness")
    public ResponseEntity<?> refresh(@PathVariable("identifier") Metadata backup) {
        backup.refresh();
        repository.save(backup);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
