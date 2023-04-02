package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingErrorResponse;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.sitemap.RecursiveIndexingTask;
import searchengine.services.sitemap.SiteMapThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private ArrayList<SiteMapThread> threads = new ArrayList<>();
    private SiteMapThread siteMapThread;

    //метод запуска индексации сайта
    @Override
    public IndexingResponse startIndexing() {

        //создание списка сайтов из application.yml
        List<Site> sitesList = sites.getSites();

        System.out.println("Кол-во сайтов - " + sitesList.size());

        for (int i=0; i < sitesList.size(); i++) {

            int num = i;

            //создали объект site с полями name и url
            Site site = sitesList.get(num);
            threads.add(new SiteMapThread(site, pageRepository, siteRepository));
        }

        for (SiteMapThread thread : threads) {
            thread.start();
        }

        if (sitesList.size() == 4) {
            return new IndexingResponse();
        } else {
            return new IndexingErrorResponse("ОШИБКА");
        }
    }

    @Override
    public IndexingErrorResponse stopIndexing() {

        System.out.println("Зашли в метод стоп-индексинг");

        for (SiteMapThread thread : threads) {
            thread.interrupt();
        }


        return new IndexingErrorResponse("Остановка индексации сайта");
    }

    private void createSiteMap(String link, SiteEntity siteEntity) {

       new ForkJoinPool().invoke(new RecursiveIndexingTask(siteEntity, siteEntity.getUrl(), siteRepository, pageRepository));
//        new ForkJoinPool().execute(new RecursiveIndexingTask(link, pageRepository, siteEntity));
    }
}
