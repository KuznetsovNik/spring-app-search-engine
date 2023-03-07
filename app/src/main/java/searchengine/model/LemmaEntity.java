package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Lemmas")
@NoArgsConstructor
@Getter
@Setter
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private int lemmaId;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "sites_id",nullable = false)
    private SiteEntity site;
    @Column(nullable = false,columnDefinition = "VARCHAR(255)")
    private String lemma;
    @Column(nullable = false)
    private int frequency;
    @OneToMany(mappedBy = "lemma",cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<IndexEntity> IndexesT;
}
