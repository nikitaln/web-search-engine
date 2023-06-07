package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

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
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {


    @Query(value = "SELECT lemma FROM lemma WHERE lemma LIKE :word", nativeQuery = true)
    String contains(String word);


    @Query(value = "SELECT id FROM lemma WHERE lemma LIKE :word AND site_id = :siteId", nativeQuery = true)
    int getLemmaIdOnSiteId(String word, int siteId);


    @Query(value = "SELECT frequency FROM lemma WHERE lemma LIKE :word AND site_id = :siteId", nativeQuery = true)
    int getFrequencyByLemmaAndSite(String word, int siteId);


    @Query(value = "SELECT COUNT(*) FROM lemma WHERE site_id = :siteId", nativeQuery = true)
    int getCountLemmaBySiteId(int siteId);


    @Query(value = "SELECT lemma FROM lemma WHERE lemma LIKE :lemma AND site_id =:siteId", nativeQuery = true)
    String getLemmaByLemmaAndSite(String lemma, int siteId);


    List<LemmaEntity> findBySiteEntity(SiteEntity siteEntity);
}
