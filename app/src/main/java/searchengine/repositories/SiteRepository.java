package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

@Repository
@Transactional
public interface SiteRepository extends CrudRepository<SiteEntity,Integer> {

    @Modifying
    @Query(value = "ALTER TABLE `sites` AUTO_INCREMENT = 0", nativeQuery = true)
    void resetIdOnSite();

    @Query(value = "SELECT s FROM SiteEntity s WHERE s.url = :url")
    SiteEntity findByUrl(String url);
}
