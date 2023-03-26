package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
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
            siteEntity.setText("Ошибка");
            siteEntity.setStatus(StatusType.INDEXING);

            //записали сущность в БД
            siteRepository.save(siteEntity);

            //запуск обхода всех ссылок сайта
            RecursiveIndexingTask rit = new RecursiveIndexingTask(site.getUrl(), pageRepository, siteEntity);
            ForkJoinPool fjp = new ForkJoinPool();
            fjp.invoke(rit);
        }
        IndexingResponse response = new IndexingResponse();

        if (sitesList.size() == 4) {
            response.setResult(true);
            return response;
        } else {
            response.setResult(false);
            response.setError("Индексация уже запущена");
            return response;
        }
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }
}
