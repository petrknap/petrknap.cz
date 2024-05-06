package cz.petrknap.website.backups;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "backups")
public class Metadata {
    @Id
    private String identifier;
    private LocalDateTime refreshedAt;
    @Column(nullable = false)
    private Integer freshForHours;

    protected Metadata() {
    }

    public Metadata(String identifier, Integer freshForHours) {
        this.identifier = identifier;
        setFreshForHours(freshForHours);
    }

    public String getIdentifier() {
        return identifier;
    }

    public LocalDateTime getRefreshedAt() {
        return refreshedAt;
    }

    public void setFreshForHours(Integer freshForHours) {
        this.freshForHours = Math.max(1, freshForHours);
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
