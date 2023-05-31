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
import searchengine.services.sitemap.LemmaStorage;
import searchengine.services.sitemap.SiteMapThread;
import searchengine.services.utils.EditorURL;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private SiteMapThread siteMapThread;
    private ExecutorService executorService;
    private FlagStop flagStop;

    @Override
    public IndexingResponse startIndexing() {

        flagStop = new FlagStop();

        List<Site> sitesList = sites.getSites();

        for (int i = 0; i < sitesList.size(); i++) {

            Site site = sitesList.get(i);
            if (siteContainsInDB(site.getUrl())) {

                SiteEntity siteEntity = siteRepository.getByUrl(site.getUrl());
                if (siteEntity.getStatus().equals(StatusType.INDEXING)) {

                    return new IndexingErrorResponse("Индексация уже запущена");
                } else {

                    siteRepository.delete(siteEntity);
                }
            }

            executorService = Executors.newSingleThreadExecutor();
            siteMapThread = new SiteMapThread(site, siteRepository, pageRepository, lemmaRepository, indexRepository, flagStop);
            executorService.execute(siteMapThread);
            executorService.shutdown();

        }
        return new IndexingResponse();
    }
    @Override
    public IndexingResponse stopIndexing() {

        System.out.println("-> STOP-method");

        Iterator<SiteEntity> iterator = siteRepository.findAll().iterator();

        while (iterator.hasNext()) {

            SiteEntity siteEntity = iterator.next();
            if (siteEntity.getStatus().equals(StatusType.INDEXING)) {
                flagStop.stopRecursiveTask();
                siteEntity.setStatus(StatusType.FAILED);
                siteEntity.setTime(LocalDateTime.now());
                siteEntity.setText("Остановка индексации пользователем");
                siteRepository.save(siteEntity);
                return new IndexingResponse();
            }
        }

        executorService.shutdown();

        return new IndexingErrorResponse("Индексация не запущена");
    }
    @Override
    public IndexingResponse indexPage(String url) {

        // url - https://www.playback.ru/catalog/1141.html
        // uri - /catalog/1141.html
        String uri = "";

        EditorURL editorURL = new EditorURL();
        String siteUrl = editorURL.getSiteURL(url);

        if (siteContainsInDB(siteUrl)) {

            SiteEntity siteEntity = siteRepository.getByUrl(siteUrl);
            //проверить наличие ссылки в Таблице page
            int countLetters = siteEntity.getUrl().length() - 1;
            uri = url.substring(countLetters);

            if (pageContainsInDB(uri)) {
                deletePage(uri);
            }

            LemmaStorage lemmaStorage = new LemmaStorage();
            new PageIndexing(uri, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaStorage).indexPage();
            lemmaStorage.clearMapLemmas();

            return new IndexingResponse();

            //проверка наличие сайта в конфиге
        } else if (siteInConfig(siteUrl)) {

            List<Site> sitesList = sites.getSites();

            for (int i=0; i < sitesList.size(); i++) {

                //создали объект site с полями name и url
                Site site = sitesList.get(i);

                if (siteUrl.equals(site.getUrl())) {

                    SiteEntity siteEntity = createSiteEntity(site.getName(), site.getUrl());
                    siteRepository.save(siteEntity);

                    int countLetters = siteEntity.getUrl().length() - 1;
                    uri = url.substring(countLetters);

                    LemmaStorage lemmaStorage = new LemmaStorage();
                    new PageIndexing(uri, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaStorage).indexPage();
                    lemmaStorage.clearMapLemmas();
                }
            }
            return new IndexingResponse();
        } else {
            return new IndexingErrorResponse("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n");
        }
    }

    private boolean siteInConfig(String siteUrl) {

        List<Site> sitesList = sites.getSites();
        for (int i=0; i < sitesList.size(); i++) {
            if (sitesList.get(i).getUrl().equals(siteUrl)) {
                return true;
            }
        }
        return false;


//        for (int i=0; i < sitesList.size(); i++) {
//            //создали объект site с полями name и url
//            Site site = sitesList.get(i);
//
//            String siteUrl = sitesList.get(i).getUrl();
//
//            Pattern pattern = Pattern.compile(siteUrl);
//            Matcher matcher = pattern.matcher(url);
//            while (matcher.find()) {
//                int start = matcher.start();
//                int end = matcher.end();
//                System.out.println(url.substring(start, end));
//                if (siteUrl.equals(url.substring(start, end))) {
//                    return true;
//                }
//            }
//        }
//        return false;
    }
    private void deletePage(String url) {

        if (url.equals(pageRepository.contains(url))) {
            int pageId = pageRepository.getId(url);
            List<Integer> listLemmaId = indexRepository.getAllLemmaId(pageId);
            pageRepository.deleteById(pageId);
            for (Integer lemmaId : listLemmaId) {
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

//        int countSite = siteRepository.getCount();
//
//        for (int i=1; i<=countSite; i++) {
//
//            String siteUrl = siteRepository.findById(i).get().getUrl();
//
//            Pattern pattern = Pattern.compile(siteUrl);
//            Matcher matcher = pattern.matcher(url);
//            while (matcher.find()) {
//                int start = matcher.start();
//                int end = matcher.end();
//                System.out.println(url.substring(start, end));
//                if (siteUrl.equals(url.substring(start, end))) {
//                    return true;
//                }
//            }
//        }
//        return false;

        if (urlSite.equals(siteRepository.contains(urlSite))) {
            return true;
        } else return false;
    }
    private SiteEntity createSiteEntity(String siteName, String siteURL) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setNameSite(siteName);
        siteEntity.setUrl(siteURL);
        siteEntity.setTime(LocalDateTime.now());
        siteEntity.setText(null);
        siteEntity.setStatus(StatusType.INDEXING);
        return siteEntity;
    }
    private String getSiteUrl(String url) {

        String siteUrl = "";

        List<Site> sitesList = sites.getSites();

        for (int i=0; i < sitesList.size(); i++) {
            //создали объект site с полями name и url
            String siteUrlRegex = sitesList.get(i).getUrl();

            Pattern pattern = Pattern.compile(siteUrlRegex);
            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                siteUrl = url.substring(start, end);
            }
        }
        return siteUrl;
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
