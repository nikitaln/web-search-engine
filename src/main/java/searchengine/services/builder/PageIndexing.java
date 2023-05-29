package searchengine.services.builder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.config.LemmaConfiguration;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemma.LemmaFinder;
import searchengine.services.sitemap.LemmaStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class PageIndexing {

    private String uri;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private LemmaStorage lemmaStorage;

    public PageIndexing(String uri, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, LemmaStorage lemmaStorage) {
        this.uri = uri;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaStorage = lemmaStorage;
    }

    public void indexPage()  {

        siteEntity.setTime(LocalDateTime.now());
        siteRepository.save(siteEntity);

        String fullUrl = siteEntity.getUrl() + uri.substring(1);


        try {

//            long START_3 = System.currentTimeMillis();
//            Connection.Response response = Jsoup.connect(fullUrl).followRedirects(false).execute();
//            System.out.println(response.statusCode() + " : " + response.url());
//            long FINISH_3 = System.currentTimeMillis() - START_3;
//            System.out.println("\tвремя получения ответа : " + FINISH_3);

            Document doc2 = Jsoup.connect(fullUrl).get(); // длительность почти секунда


            PageEntity pageEntity = new PageEntity();
            pageEntity.setSite(siteEntity);
            pageEntity.setCodeHTTP(doc2.connection().response().statusCode());
            pageEntity.setContent(doc2.outerHtml());
            pageEntity.setPath(uri);
            pageRepository.save(pageEntity);

            saveLemmaAndIndex(doc2.outerHtml(), pageEntity);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveLemmaAndIndex(String codeHTML, PageEntity pageEntity) {

        long start = System.currentTimeMillis();

        Set<IndexEntity> setIndexEntities = new HashSet<>();
        Set<LemmaEntity> setLemmaEntities = new HashSet<>();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());
            Map<String, Integer> map = lemmaFinder.getLemmaMapWithoutParticles(codeHTML);

            for (String key : map.keySet()) {

                if (lemmaStorage.containsKeyMapLemmas(key)) {

                    LemmaEntity lemmaEntity = updateLemmaEntity(key);
                    lemmaStorage.addLemma(key, lemmaEntity);
                    setLemmaEntities.add(lemmaEntity);

                    IndexEntity indexEntity = createIndexEntity(lemmaEntity, pageEntity, map.get(key));
                    setIndexEntities.add(indexEntity);

                } else {

                    LemmaEntity lemmaEntity = createLemmaEntity(key, 1, siteEntity);
                    lemmaStorage.addLemma(key, lemmaEntity);
                    setLemmaEntities.add(lemmaEntity);

                    IndexEntity indexEntity = createIndexEntity(lemmaEntity, pageEntity, map.get(key));
                    setIndexEntities.add(indexEntity);
                }
            }

            long start1 = System.currentTimeMillis();

            lemmaRepository.saveAll(setLemmaEntities);
            indexRepository.saveAll(setIndexEntities);

            long finish1 = System.currentTimeMillis() - start1;
            System.out.println("\ttime: saveAll: " + finish1 + " ms.");
            long finish = System.currentTimeMillis() - start;
            System.out.println("\ttime: work with lemmas: " + finish + " ms.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private IndexEntity createIndexEntity(LemmaEntity lemmaEntity, PageEntity pageEntity, int countLemma) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setPageEntity(pageEntity);
        indexEntity.setRank(countLemma);
        return indexEntity;
    }
    private LemmaEntity createLemmaEntity(String lemma, int frequency, SiteEntity siteEntity) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setLemma(lemma);
        lemmaEntity.setFrequency(frequency);
        lemmaEntity.setSiteEntity(siteEntity);
        return lemmaEntity;
    }
    private LemmaEntity updateLemmaEntity(String lemma) {
        LemmaEntity lemmaEntity = lemmaStorage.getLemmaEntity(lemma);
        int count = lemmaEntity.getFrequency();
        lemmaEntity.setFrequency(count + 1);
        return lemmaEntity;
    }

}
