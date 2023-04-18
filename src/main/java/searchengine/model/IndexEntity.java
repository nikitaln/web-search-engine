package searchengine.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity pageEntity;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false, referencedColumnName = "id")
    private LemmaEntity lemmaEntity;

    @Column(name = "`rank`", columnDefinition = "FLOAT NOT NULL")
    private float rank;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PageEntity getPageEntity() {
        return pageEntity;
    }

    public void setPageEntity(PageEntity pageEntity) {
        this.pageEntity = pageEntity;
    }

    public LemmaEntity getLemmaEntity() {
        return lemmaEntity;
    }

    public void setLemmaEntity(LemmaEntity lemmaEntity) {
        this.lemmaEntity = lemmaEntity;
    }

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }
}
