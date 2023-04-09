package searchengine.model;
import javax.persistence.*;

@Entity
@Table(name = "`lemma`")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private SiteEntity siteEntity;
    @Column(name = "lemma", columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;
    @Column(name = "frequency", columnDefinition = "INT NOT NULL")
    private int frequency;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public SiteEntity getSiteEntity() {
        return siteEntity;
    }

    public void setSiteEntity(SiteEntity siteEntity) {
        this.siteEntity = siteEntity;
    }
}
