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
import searchengine.services.utils.EditorURL;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecursiveIndexingTask extends RecursiveAction {

    private String url;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private FlagStop flagStop;
    private Storage storage;

    public RecursiveIndexingTask(String url, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, FlagStop flagStop, Storage storage) {
        this.url = url;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.flagStop = flagStop;
        this.storage = storage;
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
                        String newUrl = element.absUrl("href");
                        if (isDomainUrl(newUrl)) {
                            //получили ссылку без сайта
                            String uri = getUri(newUrl);
                            synchronized (pageRepository) {
                                if (!containsInDataBase(uri)) {
                                    new PageIndexing(uri, siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, storage)
                                            .indexPage();

                                    System.out.println("\tсохр. ссылку " + uri + " | из потока " + Thread.currentThread().getName());
                                    RecursiveIndexingTask task = new RecursiveIndexingTask(
                                            newUrl,
                                            siteEntity,
                                            siteRepository,
                                            pageRepository,
                                            lemmaRepository,
                                            indexRepository,
                                            flagStop,
                                            storage);
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
    private boolean containsInDataBase(String link) {
        if (link.equals(pageRepository.contains(link))) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isDomainUrl(String link) {
        if (link.startsWith(siteEntity.getUrl())
                && !link.contains("#")
                && !link.contains("jpg")
                && !link.contains("jpeg")
                && !link.contains("png")
                && !link.contains("doc")
                && !link.contains("docx")
                && !link.contains("*")
                && !link.contains("pdf")) {
            return true;
        } else {
            return false;
        }
    }
    private String getUri(String url) {
        int countLetters = siteEntity.getUrl().length() - 1;
        return url.substring(countLetters);
    }
    private boolean getOnlyWebPage(String url) {

        long start = System.currentTimeMillis();

        String correctType = "text";
        //https://dombulgakova.ru/?method=ical&id=5116

        try {
            String typeContent = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute().contentType();
            if (typeContent.contains(correctType)) {
                long finish = System.currentTimeMillis() - start;
                System.out.println("*время проверки веб-страницы " + finish + " мс");
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }


}