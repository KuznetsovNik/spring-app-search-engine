package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Pages")
@NoArgsConstructor
@Getter
@Setter
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private int pageId;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "sites_id",nullable = false)
    private SiteEntity site;
    @Column(columnDefinition = "TEXT NOT NULL, UNIQUE KEY pathIndex (path(512),sites_id)")
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(length = 16777215, nullable = false,columnDefinition = "MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;
    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<IndexEntity> IndexesT;
}

