package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexEntity;

@Repository
@Transactional
public interface IndexRepository extends CrudRepository<IndexEntity,Integer> {

    @Modifying
    @Query(value = "ALTER TABLE `indexest` AUTO_INCREMENT = 0", nativeQuery = true)
    void resetIdOnIndexest();

    @Modifying
    @Query(value = "DELETE FROM `indexesT` WHERE `id` = :index_id",nativeQuery = true)
    void deleteByPage(int index_id);

    @Query(value = "SELECT i FROM IndexEntity i WHERE i.id = :pageEntityId")
    IndexEntity findByPage(int pageEntityId);
}
