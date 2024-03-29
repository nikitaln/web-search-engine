package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;

/**
 * @Repository - аннотация Spring, необходима чтобы указать, что класс предоставляет механизм
 * для хранения, извлечения, обновления, удаления и поиска по объектам
 *
 * наследуем CrudRepository — это интерфейс данных Spring для общих операций CRUD
 * в репозитории определенного типа. Он предоставляет несколько готовых методов для
 * взаимодействия с базой данных.
 *
 * @Query - аннотация позволяющая писать собственный SQL-запрос,
 * nativeQuery со значение true указывает, что это собственный запрос,
 * чтобы указать в запросе входящий параметр из метода, необходимо написать ':'
 * перед именем переменной (:name)
 */

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {


    @Query(value = "SELECT `lemma_id` FROM `index` WHERE `page_id` = :pageId", nativeQuery = true)
    List<Integer> getAllLemmaId(int pageId);


    @Query(value = "SELECT `page_id` FROM `index` WHERE `lemma_id` = :lemmaId", nativeQuery = true)
    List<Integer> getAllPagesIdByLemmaId(int lemmaId);


    @Query(value = "SELECT `rank` FROM `index` WHERE `lemma_id` = :lemmaId AND `page_id` = :pageId", nativeQuery = true)
    float getRankByLemmaIdAndPageId(int lemmaId, int pageId);

}
