package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.PageEntity;

@Repository
@Transactional
public interface PageRepository extends CrudRepository<PageEntity,Integer> {
    @Modifying
    @Query(value = "ALTER TABLE `pages` AUTO_INCREMENT = 0", nativeQuery = true)
    void resetIdOnPage();

    @Query(value = "SELECT p FROM PageEntity p WHERE p.path = :path")
    PageEntity findByPath(String path);
}
