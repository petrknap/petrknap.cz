package cz.petrknap.website.link;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String slug;
    @Column(nullable = false)
    private String location;
    @Column(nullable = false)
    private Boolean forward;

    protected Link() {
    }

    public Link(String slug, String location, Boolean forward) {
        setSlug(slug);
        setLocation(location);
        setForward(forward);
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isForward()
    {
        return Boolean.TRUE.equals(forward);
    }

    public Boolean getForward()
    {
        return forward;
    }

    public void setForward(Boolean forward)
    {
        this.forward = forward;
    }
}
