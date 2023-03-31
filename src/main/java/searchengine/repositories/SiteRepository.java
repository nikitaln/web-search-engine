package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {
    @Query(value = "SELECT url FROM site WHERE url LIKE %:name%", nativeQuery = true)
    String contains(String name);

}
