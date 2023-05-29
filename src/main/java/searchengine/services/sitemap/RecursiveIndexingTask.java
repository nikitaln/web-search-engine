package searchengine.services.sitemap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

    public RecursiveIndexingTask(String url, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, FlagStop flagStop, LemmaStorage lemmaStorage) {
        this.url = url;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.flagStop = flagStop;
        this.lemmaStorage = lemmaStorage;
    }
    @Override
    protected void compute() {
        //Флажок завершения задачи
        if (!flagStop.isStopNow()) {
            ArrayList<RecursiveIndexingTask> allTasks = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select("a");
                elements.forEach(element -> {
                    if (!flagStop.isStopNow()) {
                        String newURL = element.absUrl("href");
                        if (isDomainUrl(newURL)) {
                            String uri = getURI(newURL);
                            synchronized (pageRepository) {
                                if (!containsInDB(uri)) {
                                    new PageIndexing(uri, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaStorage)
                                            .indexPage();
                                    System.out.println("save URI: " + uri + " | from thread: " + Thread.currentThread().getName());
                                    RecursiveIndexingTask task = new RecursiveIndexingTask(
                                            newURL,
                                            siteEntity,
                                            siteRepository,
                                            pageRepository,
                                            lemmaRepository,
                                            indexRepository,
                                            flagStop,
                                            lemmaStorage);
                                    task.fork();
                                    allTasks.add(task);
                                }
                            }
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (RecursiveIndexingTask task : allTasks) {
                task.join();
            }
        }
    }
    private boolean containsInDB(String url) {
        if (url.equals(pageRepository.contains(url))) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isDomainUrl(String url) {
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