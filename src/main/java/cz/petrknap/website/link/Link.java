package cz.petrknap.website.link;

import jakarta.persistence.*;

@Entity
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    @Column(unique=true, nullable = false)
    private String slug;
    @Column(nullable = false)
    private String url;

    protected Link() {
    }

    public Link(String slug, String url) {
        setSlug(slug);
        setUrl(url);
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
