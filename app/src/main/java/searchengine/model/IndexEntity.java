package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Table(name = "IndexesT")
@NoArgsConstructor
@Getter
@Setter
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private int indexId;
    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id",nullable = false,referencedColumnName = "id")
    private PageEntity page;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id",nullable = false,referencedColumnName = "id")
    private LemmaEntity lemma;
    @Column(nullable = false)
    private float rankT;
}

