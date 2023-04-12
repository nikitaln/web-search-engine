package searchengine.services.builder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;

public class PageIndexing {
    private String url;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;

    public void indexPage(String url) {

        try {
            Document doc2 = Jsoup.connect(url).get();

            PageEntity pageEntity = new PageEntity();

            int statusCode = doc2.connection().response().statusCode();

            if (statusCode > 400 && statusCode < 600) {
                pageEntity.setSite(siteEntity);
                pageEntity.setCodeHTTP(doc2.connection().response().statusCode());
                pageEntity.setContent(doc2.outerHtml());
                pageEntity.setPath(url);
                pageRepository.save(pageEntity);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
