package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SiteBuilder {


    private Site site;
    private SiteRepository siteRepository;
    private String url;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    public SiteBuilder(Site site, SiteRepository siteRepository, String url, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.site = site;
        this.siteRepository = siteRepository;
        this.url = url;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    public void siteSaveToDB() {

        SiteEntity siteEntity = new SiteEntity();

        siteEntity.setNameSite(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setTime(LocalDateTime.now());
        siteEntity.setText(null);
        siteEntity.setStatus(StatusType.INDEXING);

        siteRepository.save(siteEntity);

        pageSaveToDB(url, siteEntity);
    }

    private void pageSaveToDB(String url, SiteEntity siteEntity) {

        try {
            Document doc2 = Jsoup.connect(url).get();

            PageEntity pageEntity = new PageEntity();

            pageEntity.setSite(siteEntity);
            pageEntity.setCodeHTTP(doc2.connection().response().statusCode());
            pageEntity.setContent(doc2.outerHtml());
            pageEntity.setPath(url);

            pageRepository.save(pageEntity);

            lemmaSaveToDB(url, siteEntity, pageEntity);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void lemmaSaveToDB(String url, SiteEntity siteEntity, PageEntity pageEntity) {

        try {
            Document doc2 = Jsoup.connect(url).get();

            String htmlCode = doc2.outerHtml();

            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            LemmaFinder lemmaFinder = new LemmaFinder(luceneMorph);
            Map<String, Integer> map = new HashMap<>();

            map = lemmaFinder.getLemmaMapWithoutParticles(lemmaFinder.deleteHtmlTags(htmlCode));

            for (String key : map.keySet()) {

                LemmaEntity lemma = new LemmaEntity();
                lemma.setLemma(key);
                lemma.setFrequency(1);
                lemma.setSiteEntity(siteEntity);

                lemmaRepository.save(lemma);

                System.out.println(key + " " + map.get(key));
                saveIndex(lemma, indexRepository, pageEntity, map.get(key));

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveIndex(LemmaEntity lemmaEntity, IndexRepository indexRepository, PageEntity pageEntity, float rank) {

        System.out.println("зашли в метод saveIndex");

        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setRank(rank);
        indexEntity.setPageEntity(pageEntity);

        indexRepository.save(indexEntity);

    }
}
