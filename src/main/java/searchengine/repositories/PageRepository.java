package searchengine.repositories;

import lombok.Synchronized;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

@Repository
public interface PageRepository extends CrudRepository<PageEntity, Integer> {

    @Query(value = "SELECT path FROM page WHERE path LIKE :link", nativeQuery = true)
    String contains(String link);
}
