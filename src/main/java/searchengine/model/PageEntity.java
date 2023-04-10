package searchengine.model;

import javax.persistence.*;

@Entity
@Table(name = "Page")
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    @Column(name = "path", columnDefinition = "TEXT NOT NULL")
    private String path;

    @Column(name = "code", columnDefinition = "INT NOT NULL")
    private int codeHTTP;

    @Column(name = "content", columnDefinition = "LONGTEXT NOT NULL")
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SiteEntity getSite() {
        return siteEntity;
    }

    public void setSite(SiteEntity siteEntity) {
        this.siteEntity = siteEntity;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCodeHTTP() {
        return codeHTTP;
    }

    public void setCodeHTTP(int codeHTTP) {
        this.codeHTTP = codeHTTP;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
