package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingErrorResponse;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.builder.PageIndexing;
import searchengine.services.sitemap.FlagStop;
import searchengine.services.sitemap.SiteMapThread;
import searchengine.utils.EditorURL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Service - анотация Spring, показывает, что наш класс является сервисом,
 * и выполняет функцию поставщиком услуг
 *
 * @RequiredArgsConstructor - анотация библиотеки Lombok, сокращает шаблонный
 * код и генерирует конструктор из полей final
 *
 */

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
    private FlagStop flagStop = new FlagStop();

    //метод запуска индексации сайта
    @Override
    public IndexingResponse startIndexing() {

        List<Site> sitesList = sites.getSites();

        for (int i = 0; i < sitesList.size(); i++) {

            Site site = sitesList.get(i);

            //в процессе
            if (siteContainsInDB(site.getUrl())) {
                SiteEntity siteEntity = siteRepository.getByUrl(site.getUrl());
                if (siteEntity.equals(StatusType.INDEXED)) {
                    siteRepository.delete(siteEntity);
                } else if (siteEntity.equals(StatusType.INDEXING)) {
                    return new IndexingErrorResponse("Индексация уже запущена");
                }
            }
            executorService = Executors.newSingleThreadExecutor();
            siteMapThread = new SiteMapThread(site, siteRepository, pageRepository, lemmaRepository, indexRepository, flagStop);
            executorService.execute(siteMapThread);
        }
        return new IndexingResponse();
    }

    @Override
    public IndexingErrorResponse stopIndexing() {

        System.out.println("Вошли в стоп");
        flagStop.stopRecursiveTask();

        Iterator<SiteEntity> iterator = siteRepository.findAll().iterator();

        while (iterator.hasNext()) {
            SiteEntity siteEntity = iterator.next();
            siteEntity.setStatus(StatusType.FAILED);
            siteEntity.setTime(LocalDateTime.now());
            siteEntity.setText("Stop by user");
            siteRepository.save(siteEntity);
        }

        executorService.shutdown();

        return new IndexingErrorResponse("Остановка индексации сайта");
    }

    @Override
    public IndexingResponse indexPage(String url) {

        EditorURL editorURL = new EditorURL();
        String siteUrl = editorURL.getSiteURL(url);

        //проверить наличие сайта в БД
        if (siteContainsInDB(siteUrl)) {

            SiteEntity siteEntity = siteRepository.findById(siteRepository.getId(siteUrl)).get();

            //проверить наличие ссылки в Таблице page
            if (pageContainsInDB(url)) {
                deletePage(url);
            }
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
                }
            }
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

            List<Integer> listLemmaId = new ArrayList<>();

            listLemmaId = indexRepository.getAllLemmaId(pageId);

            pageRepository.deleteById(pageId);

            for (Integer lemmaId : listLemmaId) {
                System.out.println(lemmaId);
                LemmaEntity lemmaEntity = lemmaRepository.findById(lemmaId).get();
                int freq = lemmaEntity.getFrequency();
                if (freq != 1) {
                    lemmaEntity.setFrequency(freq - 1);
                } else {
                    lemmaRepository.delete(lemmaEntity);
                }
            }
        }
    }

    private boolean pageContainsInDB(String url) {

        if (url.equals(pageRepository.contains(url))) {
            return true;
        } else return false;
    }

    private boolean siteContainsInDB(String urlSite) {

        if (urlSite.equals(siteRepository.contains(urlSite))) {
            return true;
        } else return false;
    }


    private void shutdownAndAwaitTermination(ExecutorService pool) {
        // Disable new tasks from being submitted
        pool.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks forcefully
                pool.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ex) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
