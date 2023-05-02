package searchengine.services.sitemap;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.LemmaConfiguration;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.builder.PageIndexing;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecursiveIndexingTask extends RecursiveAction {

    private String link;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private FlagStop flagStop;

    public RecursiveIndexingTask(String link,
                                 SiteEntity siteEntity,
                                 SiteRepository siteRepository,
                                 PageRepository pageRepository,
                                 LemmaRepository lemmaRepository,
                                 IndexRepository indexRepository,
                                 FlagStop flagStop) {
        this.link = link;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.flagStop = flagStop;
    }

    @Override
    protected void compute() {

        //Флажок завершения задачи
        if (!flagStop.isStopNow()) {

            ArrayList<RecursiveIndexingTask> allTasks = new ArrayList<>();

            try {
                Connection.Response response = Jsoup.connect(link).execute();
                Document doc = response.parse();
                Elements elements = doc.select("a");
                elements.forEach(element -> {

                    if (!flagStop.isStopNow()) {

                        String newLink = element.absUrl("href");

                        if (isDomainUrl(newLink)) {
                            String newLink1 = deleteUrl(newLink);
                            synchronized (pageRepository) {

                                if (!containsInDataBase(newLink1)) {

                                    savePage(newLink1);
                                    System.out.println("сохр. ссылку " + newLink1 + " | из потока " + Thread.currentThread().getName());

                                    RecursiveIndexingTask task = new RecursiveIndexingTask(
                                            newLink,
                                            siteEntity,
                                            siteRepository,
                                            pageRepository,
                                            lemmaRepository,
                                            indexRepository,
                                            flagStop);
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
    public boolean containsInDataBase(String link) {
        if (link.equals(pageRepository.contains(link))) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isDomainUrl(String link) {
        if (link.startsWith(siteEntity.getUrl())
                && !link.contains("#")
                && !link.contains("jpg")
                && !link.contains("jpeg")
                && !link.contains("doc")
                && !link.contains("docx")
                && !link.contains("*")
                && !link.contains("pdf")) {
            return true;
        } else {
            return false;
        }
    }

    public void savePage(String url) {
        new PageIndexing(url, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository).indexPage();
    }

    private String deleteUrl(String url) {
        int countLetters = siteEntity.getUrl().length() - 1;
        return url.substring(countLetters);
    }
}