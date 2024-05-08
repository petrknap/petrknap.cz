package cz.petrknap.website;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

public abstract class JpaCrudController<T, ID> {
    protected JpaRepository<T, ID> repository;

    @Operation(summary = "List")
    @GetMapping("/")
    public List<T> list() {
        return repository.findAll();
    }

    @Operation(summary = "Create")
    @PostMapping("/")
    @ApiResponse(responseCode = "201", headers = {@Header(name = HttpHeaders.LOCATION, description = "Relative link to the created entity")})
    @ApiResponse(responseCode = "400", content = @Content)
    @ApiResponse(responseCode = "409", content = @Content)
    public ResponseEntity<T> create(@RequestBody T requested) {
        ID id;
        try {
            id = doCreate(requested);
        } catch (ResponseStatusException e) {
            throw e;
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, id.toString())
                .body(repository.findById(id).orElseThrow());
    }

    @Operation(summary = "Read")
    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", content = @Content)
    public T read(@PathVariable ID id) {
        return getEntityById(id);
    }

    @Operation(summary = "Update")
    @PutMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "400", content = @Content)
    @ApiResponse(responseCode = "404", content = @Content)
    public ResponseEntity<T> update(@PathVariable ID id, @RequestBody T requested) {
        try {
            return ResponseEntity.ok(doUpdate(getEntityById(id), requested));
        } catch (ResponseStatusException e) {
            throw e;
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Operation(summary = "Delete")
    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", content = @Content)
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        repository.delete(getEntityById(id));

        return ResponseEntity.noContent().build();
    }

    protected void throwConflictIfPresent(Optional<T> entity) throws ResponseStatusException {
        if (entity.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    protected abstract ID doCreate(T requested) throws Exception;
    protected abstract T doUpdate(T actual, T requested) throws Exception;

    private T getEntityById(ID id) throws ResponseStatusException {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
