package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {
}
