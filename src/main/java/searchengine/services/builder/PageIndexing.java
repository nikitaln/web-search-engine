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
import searchengine.services.sitemap.Storage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class PageIndexing {
    private String url;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private Storage storage;

    public PageIndexing(String url, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, Storage storage) {
        this.url = url;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.storage = storage;
    }

    public void indexPage() {

        siteEntity.setTime(LocalDateTime.now());
        siteRepository.save(siteEntity);

        String url2 = siteEntity.getUrl() + url.substring(1);

        try {
            Document doc2 = Jsoup.connect(url2).get();

            PageEntity pageEntity = new PageEntity();

            int statusCode = doc2.connection().response().statusCode();

            if (statusCode < 400) {
                pageEntity.setSite(siteEntity);
                pageEntity.setCodeHTTP(statusCode);
                pageEntity.setContent(doc2.outerHtml());
                pageEntity.setPath(url);
                pageRepository.save(pageEntity);

                saveLemma(doc2.outerHtml(), pageEntity);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveLemma(String codeHTML, PageEntity pageEntity) {

        long start = System.currentTimeMillis();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());

            Map<String, Integer> map = new HashMap<>();

//            map = lemmaFinder.getLemmaMapWithoutParticles(lemmaFinder.deleteHtmlTags(codeHTML));
            map = lemmaFinder.getLemmaMapWithoutParticles(codeHTML);

            Set<LemmaEntity> lemmas = new HashSet<>();
            Set<IndexEntity> indexes = new HashSet<>();

            Map<String, Integer> mapLemmas = new HashMap<>();

            //========TESTING CODE=========

            for (String key : map.keySet()) {

                if (storage.lemmas.containsKey(key)) {

                    LemmaEntity lemmaEntity = storage.lemmas.get(key);
                    int count = lemmaEntity.getFrequency();
                    lemmaEntity.setFrequency(count + 1);

                    storage.lemmas.put(key, lemmaEntity);

                    IndexEntity indexEntity = new IndexEntity();
                    indexEntity.setLemmaEntity(lemmaEntity);
                    indexEntity.setPageEntity(pageEntity);
                    indexEntity.setRank(map.get(key));

                    indexes.add(indexEntity);

                } else {

                    int count2 = 1;
                    storage.addLemma(key, count2);

                    LemmaEntity lemmaEntity = new LemmaEntity();
                    lemmaEntity.setLemma(key);
                    lemmaEntity.setFrequency(count2);
                    lemmaEntity.setSiteEntity(siteEntity);

                    storage.lemmas.put(key, lemmaEntity);

                    IndexEntity indexEntity = new IndexEntity();
                    indexEntity.setLemmaEntity(lemmaEntity);
                    indexEntity.setPageEntity(pageEntity);
                    indexEntity.setRank(map.get(key));

                    indexes.add(indexEntity);

                }
            }

            for (String key : storage.lemmas.keySet()) {
                lemmas.add(storage.lemmas.get(key));
            }


            lemmaRepository.saveAll(lemmas);
            indexRepository.saveAll(indexes);

            //=======TESTING END===========

            long finish = System.currentTimeMillis() - start;

            System.out.println("\tдлительность операции с леммами - " + finish + " мс.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveIndex(int countWord, LemmaEntity lemmaEntity, PageEntity pageEntity) {

        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setRank(countWord);
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setPageEntity(pageEntity);

        indexRepository.save(indexEntity);

    }
}
