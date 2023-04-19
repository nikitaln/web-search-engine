package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {
    @Query(value = "SELECT lemma FROM lemma WHERE lemma LIKE :word", nativeQuery = true)
    String contains(String word);

    @Query(value = "SELECT id FROM lemma WHERE lemma LIKE :word", nativeQuery = true)
    int getLemmaId(String word);
    @Query(value = "SELECT frequency FROM lemma WHERE lemma LIKE :word", nativeQuery = true)
    int getFrequencyByLemma(String word);
}
