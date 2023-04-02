package searchengine.services.sitemap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

public class RecursiveIndexingTask extends RecursiveAction {

    private SiteEntity siteEntity;
    private String link;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;

    public RecursiveIndexingTask(SiteEntity siteEntity, String link, SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteEntity = siteEntity;
        this.link = link;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    protected void compute() {

        ArrayList<RecursiveIndexingTask> allTasks = new ArrayList<>();

        try {
            Connection.Response response = Jsoup.connect(link).execute();
            Document doc = response.parse();
            Elements elements = doc.select("a");
            elements.forEach(element -> {
                String newLink = element.absUrl("href");

                if (isDomainUrl(newLink)) {

                    synchronized (pageRepository) {
                        if (!containsInDataBase(newLink)) {

                            savePage(newLink);

                            System.out.println(newLink + " " + Thread.currentThread().getName());
                            RecursiveIndexingTask task = new RecursiveIndexingTask(
                                    siteEntity,
                                    newLink,
                                    siteRepository,
                                    pageRepository);
                            task.fork();
                            allTasks.add(task);
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

    public void savePage(String link) {

        try {
            Document doc2 = Jsoup.connect(link).get();

            PageEntity pageEntity = new PageEntity();
            pageEntity.setSite(siteEntity);
            pageEntity.setCodeHTTP(doc2.connection().response().statusCode());
            pageEntity.setContent(doc2.outerHtml());
            pageEntity.setPath(link);

            pageRepository.save(pageEntity);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//      парсинг HTML кода от эмоджи и тд
//    public String checkHtmlCode(String htmlCode) {
//
//        String regex = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s\\p{Punct}]";
//        String codeHtlmBeforeCheck = htmlCode.replaceAll(regex, "");
//        return codeHtlmBeforeCheck;
//    }
}