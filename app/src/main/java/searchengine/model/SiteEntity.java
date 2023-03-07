package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Sites")
@NoArgsConstructor
@Getter
@Setter
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private int siteId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private Status status;
    @Column(name = "status_time",nullable = false,columnDefinition = "DATETIME")
    private LocalDateTime statusTime;
    @Column(name = "last_error",columnDefinition = "TEXT")
    private String lastError;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "site", cascade  = CascadeType.REMOVE, orphanRemoval = true)
    private List<PageEntity> indexPage;
    @OneToMany(mappedBy = "site", cascade  = CascadeType.REMOVE, orphanRemoval = true)
    private List<LemmaEntity> indexLemma;
}

