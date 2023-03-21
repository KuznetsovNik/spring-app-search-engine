package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;
import java.util.List;

@Repository
@Transactional
public interface IndexRepository extends CrudRepository<IndexEntity,Integer> {

    @Modifying
    @Query(value = "ALTER TABLE `indexest` AUTO_INCREMENT = 0", nativeQuery = true)
    void resetIdOnIndexest();

    @Modifying
    @Query(value = "DELETE FROM `indexesT` WHERE `page_id` = :page_id",nativeQuery = true)
    void multiDeleteByPage(int page_id);

    @Query(value = "SELECT * FROM `indexesT` WHERE `page_id` = :pageId AND `lemma_id` = :lemmaId", nativeQuery = true)
    IndexEntity findByPageIdAndLemmaId(int pageId, int lemmaId);

    @Query(value = "SELECT * FROM `indexesT` WHERE `page_id` = :pageId", nativeQuery = true)
    List<IndexEntity> findByPageId(int pageId);

    @Query(value = "SELECT `page_id` FROM `indexesT` WHERE `lemma_Id` = :lemmaId", nativeQuery = true)
    List<Integer> findByLemmaId(int lemmaId);
}
