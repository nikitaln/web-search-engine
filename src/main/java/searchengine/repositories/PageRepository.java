package searchengine.repositories;

import lombok.Synchronized;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

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
public interface PageRepository extends CrudRepository<PageEntity, Integer> {

    @Query(value = "SELECT path FROM page WHERE path LIKE :link", nativeQuery = true)
    String contains(String link);
    @Query(value = "SELECT id FROM page WHERE path LIKE %:url%", nativeQuery = true)
    int getId(String url);

    @Query(value = "SELECT COUNT(*) FROM page", nativeQuery = true)
    int getCount();

    @Query(value = "SELECT COUNT(*) FROM page WHERE site_id = :siteId", nativeQuery = true)
    int getCountPagesBySiteId(int siteId);

}
