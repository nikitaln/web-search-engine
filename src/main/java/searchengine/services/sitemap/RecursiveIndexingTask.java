package searchengine.services.sitemap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.UserAgent;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.builder.PageIndexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

public class RecursiveIndexingTask extends RecursiveAction {

    private String url;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private FlagStop flagStop;
    private LemmaStorage lemmaStorage;
    private Object lock;
    private Connection.Response response;
    private UserAgent userAgent = new UserAgent();
    private Logger logger = LogManager.getLogger(RecursiveIndexingTask.class);



    public RecursiveIndexingTask(String url, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, FlagStop flagStop, LemmaStorage lemmaStorage, Object lock) {
        this.url = url;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.flagStop = flagStop;
        this.lemmaStorage = lemmaStorage;
        this.lock = lock;
    }


    @Override
    protected void compute() {

        ArrayList<RecursiveIndexingTask> allTasks = new ArrayList<>();

        try {

            response = Jsoup.connect(url)
                    .userAgent(userAgent.getUSER_AGENT())
                    .referrer(userAgent.getREFERRER())
                    .followRedirects(false).execute();

            Document doc = response.parse();
            Elements elements = doc.select("a");
            elements.forEach(element -> {
                //РЕМОНТ-------->
                String newURL = element.absUrl("href");
                if (!flagStop.isStopNow() && isDomainURL(newURL)) {
                    String uri = getURI(newURL);
                    synchronized (lock) {
                        if (!containsInDB(uri)) {
                            new PageIndexing(uri, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaStorage)
                                    .indexPage();
                            logger.info("URL added: " + newURL);
                            RecursiveIndexingTask task = new RecursiveIndexingTask(newURL, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, flagStop, lemmaStorage, lock);
                            task.fork();
                            allTasks.add(task);
                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Code response web-page: " + response.statusCode());
        }
        for (RecursiveIndexingTask task : allTasks) {
            task.join();
        }
    }


    private boolean containsInDB(String url) {
        if (url.equals(pageRepository.contains(url))) {
            return true;
        } else {
            return false;
        }
    }


    private boolean isDomainURL(String url) {
        if (url.startsWith(siteEntity.getUrl())
                && !url.contains("#")
                && !url.contains("jpg")
                && !url.contains("jpeg")
                && !url.contains("png")
                && !url.contains("doc")
                && !url.contains("docx")
                && !url.contains("*")
                && !url.contains("pdf")) {
            return true;
        } else {
            return false;
        }
    }


    private String getURI(String url) {
        int countLetters = siteEntity.getUrl().length() - 1;
        return url.substring(countLetters);
    }
}