package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingErrorResponse;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    //метод запуска индексации сайта
    @Override
    public IndexingResponse startIndexing() {

        long start = System.currentTimeMillis();

        //создание списка сайтов из application.yml
        List<Site> sitesList = sites.getSites();

        for (int i=0; i < sitesList.size(); i++) {

            //создали объект сайт с полями name и url
            Site site = sitesList.get(i);

            //создали сущность сайт для вставки в БД
            SiteEntity siteEntity = new SiteEntity();

            siteEntity.setNameSite(site.getName());
            siteEntity.setUrl(site.getUrl());
            siteEntity.setTime(LocalDateTime.now());
            siteEntity.setText(null);
            siteEntity.setStatus(StatusType.INDEXING);

            //записали сущность в БД
            siteRepository.save(siteEntity);

            RecursiveIndexingTask recursiveIndexingTask = new RecursiveIndexingTask(siteEntity, siteEntity.getUrl(), siteRepository, pageRepository);
            ForkJoinPool fjp = new ForkJoinPool();
            fjp.invoke(recursiveIndexingTask);


            siteEntity.setStatus(StatusType.INDEXED);
            siteRepository.save(siteEntity);



            long finish = System.currentTimeMillis() - start;
            System.out.println("Time process - " + finish + " ms");

            String n1 = site.getUrl();
            String n2 = siteRepository.contains(n1);

            if (n1.equals(n2)) {
                System.out.println("Совпадение найдено");
            } else {
                System.out.println("Нет совпадений");
            }
        }

        if (sitesList.size() == 4) {
            return new IndexingResponse();
        } else {
            return new IndexingErrorResponse("ОШИБКА");
        }
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }

    private void createSiteMap(String link, SiteEntity siteEntity) {

       new ForkJoinPool().invoke(new RecursiveIndexingTask(siteEntity, siteEntity.getUrl(), siteRepository, pageRepository));
//        new ForkJoinPool().execute(new RecursiveIndexingTask(link, pageRepository, siteEntity));
    }


}
