package searchengine.model;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false, referencedColumnName = "id")
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

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }
}
