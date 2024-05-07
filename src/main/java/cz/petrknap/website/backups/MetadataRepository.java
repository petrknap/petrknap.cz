package cz.petrknap.website.backups;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, String> {
    Optional<Metadata> findByIdentifier(String identifier);
}
