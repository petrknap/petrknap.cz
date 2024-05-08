package cz.petrknap.website.backups;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "backups")
public class Metadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String identifier;
    private LocalDateTime refreshedAt;
    @Column(nullable = false)
    @Min(1)
    private Integer freshForHours;

    protected Metadata() {
    }

    public Metadata(String identifier, Integer freshForHours) {
        this.identifier = identifier;
        setFreshForHours(freshForHours);
    }

    public UUID getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public LocalDateTime getRefreshedAt() {
        return refreshedAt;
    }

    public void setFreshForHours(Integer freshForHours) {
        this.freshForHours = freshForHours;
    }

    public Integer getFreshForHours() {
        return freshForHours;
    }

    public void refresh() {
        refreshedAt = LocalDateTime.now();
    }

    public boolean isFresh() {
        return refreshedAt != null && LocalDateTime.now().minusHours(freshForHours).isBefore(refreshedAt);
    }
}
