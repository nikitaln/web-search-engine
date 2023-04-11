package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
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
import searchengine.services.sitemap.SiteMapThread;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        //выбрать из строки только адрес сайта
        //проверить есть ли он в БД если нет то создать новую запись сайта

        //получить из ссылки название сайта
        String siteUrl = getSiteUrl(url);

        //проверить наличие в БД
        if (siteUrl.equals(siteRepository.contains(siteUrl))) {
            //данный сайт есть в таблице
           int id = siteRepository.getId(siteUrl);
            Optional<SiteEntity> site = siteRepository.findById(id);

            SiteEntity siteEntity = site.get();

            try {

                Document doc2 = Jsoup.connect(url).get();

                PageEntity pageEntity = new PageEntity();

                pageEntity.setSite(siteEntity);
                pageEntity.setCodeHTTP(doc2.connection().response().statusCode());
                pageEntity.setContent(doc2.outerHtml());
                pageEntity.setPath(url);

                pageRepository.save(pageEntity);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }



///==========РАБОТА НАД ДАННЫМ КОДОМ

            System.out.println("сайт в БД");
            return new IndexingResponse();

        } else if (siteInConfig(siteUrl)) {

            //сайта нет в таблице, есть в конфиге
            System.out.println("сайт в конфиг");

            List<Site> sitesList = sites.getSites();

            for (int i=0; i < sitesList.size(); i++) {

                //создали объект site с полями name и url
                Site site = sitesList.get(i);

                if (siteUrl.equals(site.getUrl())) {

                    SiteBuilder siteBuilder = new SiteBuilder(site, siteRepository, url, pageRepository, lemmaRepository, indexRepository);
                    siteBuilder.siteSaveToDB();

                    break;
                };
            }

            System.out.printf("Сайт в БД");

            //создать сущность Сайт и добавить в БД
            //создать сущность Страница и добавить в БД
            //взять HTML-код и удалить все Теги, затем Найти все Леммы
            //создать сущность Леммы и добавить в БД
            //создать сущность Индекс и добавить в БД



            return new IndexingResponse();
        } else {
            return new IndexingErrorResponse("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n");
        }
    }

    //получение главной страницы сайта из ссылки
    private String getSiteUrl(String url) {

        int numLetter = 0;
        int thirdSymbol = 0;
        char symbol = '/';

        for (int i=0; i<url.length(); i++) {
            if (url.charAt(i) == symbol) {
                numLetter++;
                if (numLetter == 3) {
                    thirdSymbol = i + 1;
                    break;
                }
            }
        }
        return url.substring(0, thirdSymbol);
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
}
