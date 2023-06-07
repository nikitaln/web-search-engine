package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

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
public interface SiteRepository extends CrudRepository<SiteEntity, Integer> {


    @Query(value = "SELECT url FROM site WHERE url LIKE %:name%", nativeQuery = true)
    String contains(String name);


    @Query(value = "SELECT id FROM site WHERE url LIKE %:url%", nativeQuery = true)
    int getId(String url);


    SiteEntity getByUrl(String url);

}
