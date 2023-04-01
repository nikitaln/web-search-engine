package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingErrorResponse;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private SiteIndexing siteIndexing;
    private ArrayList<Thread> threads = new ArrayList<>();

    //метод запуска индексации сайта
    @Override
    public IndexingResponse startIndexing() {

        //создание списка сайтов из application.yml
        List<Site> sitesList = sites.getSites();

        for (int i=0; i < sitesList.size(); i++) {
            int num = i;
            //создаем первый поток для сайта из списка
            //создали объект site с полями name и url
            Site site = sitesList.get(num);
            threads.add(new SiteIndexing(site, pageRepository, siteRepository));
            threads.get(i).start();
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

        for (Thread thread : threads) {
            thread.interrupt();
        }

        return new IndexingErrorResponse("Остановка индексации сайта");
    }

    private void createSiteMap(String link, SiteEntity siteEntity) {

       new ForkJoinPool().invoke(new RecursiveIndexingTask(siteEntity, siteEntity.getUrl(), siteRepository, pageRepository));
//        new ForkJoinPool().execute(new RecursiveIndexingTask(link, pageRepository, siteEntity));
    }
}
