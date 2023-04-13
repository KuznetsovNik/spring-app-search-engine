package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

@Repository
@Transactional
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {

    @Modifying
    @Query(value = "ALTER TABLE `lemmas` AUTO_INCREMENT = 0", nativeQuery = true)
    void resetIdOnLemmas();

    @Query(value = "SELECT * FROM `lemmas` WHERE `lemma` = :lemma AND `sites_id` = :siteId",nativeQuery = true)
    LemmaEntity findByLemmaAndSiteId(String lemma, int siteId);
}
