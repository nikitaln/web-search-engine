package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingErrorResponse;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.builder.PageIndexing;
import searchengine.services.sitemap.SiteMapThread;
import searchengine.utils.EditorURL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private ArrayList<SiteMapThread> threads = new ArrayList<>();
    private SiteMapThread siteMapThread;
    private ExecutorService executorService;







    //метод запуска индексации сайта
    @Override
    public IndexingResponse startIndexing() {

        //создание списка сайтов из application.yml
        List<Site> sitesList = sites.getSites();

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
        executorService.shutdownNow();
        System.out.println("Поток завершен");


        return new IndexingErrorResponse("Остановка индексации сайта");
    }








    @Override
    public IndexingResponse indexPage(String url) {

        EditorURL editorURL = new EditorURL();
        String siteUrl = editorURL.getSiteURL(url);

        //проверить наличие сайта в БД
        if (siteUrl.equals(siteRepository.contains(siteUrl))) {

            Optional<SiteEntity> site = siteRepository.findById(siteRepository.getId(siteUrl));
            SiteEntity siteEntity = site.get();

            deletePage(url);

            new PageIndexing(url, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository).indexPage();

            return new IndexingResponse();

            //проверка наличие сайта в конфиге
        } else if (siteInConfig(siteUrl)) {

            List<Site> sitesList = sites.getSites();

            for (int i=0; i < sitesList.size(); i++) {

                //создали объект site с полями name и url
                Site site = sitesList.get(i);

                if (siteUrl.equals(site.getUrl())) {

                    SiteEntity siteEntity = new SiteEntity();
                    siteEntity.setNameSite(site.getName());
                    siteEntity.setUrl(site.getUrl());
                    siteEntity.setTime(LocalDateTime.now());
                    siteEntity.setText(null);
                    siteEntity.setStatus(StatusType.INDEXING);
                    siteRepository.save(siteEntity);

                    PageIndexing pageIndexing = new PageIndexing(url, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository);
                    pageIndexing.indexPage();

                    break;
                };
            }
            return new IndexingResponse();
        } else if (url.equals("del")) {

            System.out.println("Зашли");

            deletePage(url);

            return new IndexingResponse();
        } else {
            return new IndexingErrorResponse("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n");
        }
    }

    private boolean siteInConfig(String url) {

        List<Site> sitesList = sites.getSites();

        for (int i=0; i < sitesList.size(); i++) {
            //создали объект site с полями name и url
            Site site = sitesList.get(i);
            if (url.equals(site.getUrl())) {
                return true;
            }
        }
        return false;
    }

    private void deletePage(String url) {
        String text = "https://www.playback.ru/catalog/1141.html";
        if (text.equals(pageRepository.contains(text))) {
            int pageId = pageRepository.getId(text);

            System.out.println("Удаляю");
            Optional<PageEntity> pageEntity = pageRepository.findById(pageId);
            pageRepository.deleteById(pageId);

        }
    }
}
