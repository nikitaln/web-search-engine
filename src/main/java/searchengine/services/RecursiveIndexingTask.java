package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

public class RecursiveIndexingTask extends RecursiveAction {

    private String link;
    private PageRepository pageRepository;
    private SiteEntity siteEntity;

    public RecursiveIndexingTask(String link, PageRepository pageRepository, SiteEntity siteEntity) {
        this.link = link;
        this.pageRepository = pageRepository;
        this.siteEntity = siteEntity;
    }

    public RecursiveIndexingTask(String link) {
        this.link = link;
    }

    @Override
    protected void compute() {

        ArrayList<RecursiveIndexingTask> listTasks = new ArrayList<>();

        try {
            Thread.sleep(150);
            Document doc = Jsoup.connect(link).get();
            Elements elements = doc.select("a");
            elements.forEach(element -> {

                String path = element.absUrl("href");

                if (path.startsWith("https://lenta.ru/") &&
                        !path.contains("#")
                        && !path.contains("?")
                        && !path.contains("%")
                        && !path.contains(".pdf")
                        && !path.contains(".jpg")
                        && !path.contains(".doc")
                        && !path.contains(".docx")
                        && !path.contains(".jpeg")) {

                    try {
                        Document doc2 = Jsoup.connect(path).get();
                        String html = checkHtmlCode(doc2.html());
                        PageEntity page = new PageEntity();
                        page.setSite(siteEntity);
                        page.setPath(path);
                        page.setCodeHTTP(200);
                        page.setContent(html);


                        RecursiveIndexingTask task = new RecursiveIndexingTask(path);
                        task.fork();
                        listTasks.add(task);
                        System.out.println(Thread.currentThread().getName());
                        pageRepository.save(page);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (RecursiveIndexingTask task : listTasks) {
            task.join();
        }

    }

    public String checkHtmlCode(String htmlCode) {

        String regex = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s\\p{Punct}]";
        String codeHtlmBeforeCheck = htmlCode.replaceAll(regex,"");

        return codeHtlmBeforeCheck;
    }

    public void containsDb() {

    }
}
