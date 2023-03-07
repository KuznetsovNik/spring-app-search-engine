package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaEntity;

@Repository
@Transactional
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {

    @Modifying
    @Query(value = "ALTER TABLE `lemmas` AUTO_INCREMENT = 0", nativeQuery = true)
    void resetIdOnLemmas();

    @Modifying
    @Query(value = "UPDATE `lemmas` SET `frequency` = :newValueFrequency WHERE `id` = :lemmaId", nativeQuery = true)
    void updateFrequency(float newValueFrequency,int lemmaId);

    @Query(value = "SELECT l FROM LemmaEntity l WHERE l.lemma = :lemma")
    LemmaEntity findByLemma(String lemma);

    @Query(value = "SELECT * FROM `lemmas` WHERE `lemma` = :lemma AND `sites_id` = :siteId",nativeQuery = true)
    LemmaEntity findByLemmaAndSiteId(String lemma, int siteId);
}
