package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    @Query(value = "SELECT url FROM site WHERE url LIKE %:name%", nativeQuery = true)
    String contains(String name);

    @Query(value = "SELECT id FROM site WHERE url LIKE %:url%", nativeQuery = true)
    int getId(String url);

}
