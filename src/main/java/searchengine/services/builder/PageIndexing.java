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
import searchengine.services.utils.EditorURL;

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
    private Storage storage;

    public PageIndexing(String uri, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, Storage storage) {
        this.uri = uri;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.storage = storage;
    }

    public void indexPage()  {
        siteEntity.setTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
        String fullUrl = siteEntity.getUrl() + uri.substring(1);
        try {
            Document doc2 = Jsoup.connect(fullUrl).get();
            PageEntity pageEntity = new PageEntity();
            EditorURL editorURL = new EditorURL();
            String html = editorURL.removeEmojiFromText(doc2.outerHtml());
            int statusCode = doc2.connection().response().statusCode();
            pageEntity.setSite(siteEntity);
            pageEntity.setCodeHTTP(statusCode);
            pageEntity.setContent(html);
            pageEntity.setPath(uri);
            pageRepository.save(pageEntity);
            saveLemma(doc2.outerHtml(), pageEntity);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveLemma(String codeHTML, PageEntity pageEntity) {

        long start = System.currentTimeMillis();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());

            Map<String, Integer> map = new HashMap<>();

            map = lemmaFinder.getLemmaMapWithoutParticles(codeHTML);

            Set<IndexEntity> indexes = new HashSet<>();

            Set<LemmaEntity> lemmaSET = new HashSet<>();

            //========TESTING CODE=========

            for (String key : map.keySet()) {

                if (storage.lemmas.containsKey(key)) {

                    LemmaEntity lemmaEntity = storage.lemmas.get(key);
                    int count = lemmaEntity.getFrequency();
                    lemmaEntity.setFrequency(count + 1);

                    storage.lemmas.put(key, lemmaEntity);
                    lemmaSET.add(lemmaEntity);

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
                    lemmaSET.add(lemmaEntity);

                    IndexEntity indexEntity = new IndexEntity();
                    indexEntity.setLemmaEntity(lemmaEntity);
                    indexEntity.setPageEntity(pageEntity);
                    indexEntity.setRank(map.get(key));

                    indexes.add(indexEntity);

                }
            }

            long start1 = System.currentTimeMillis();
            lemmaRepository.saveAll(lemmaSET);
            indexRepository.saveAll(indexes);
            long finish1 = System.currentTimeMillis() - start1;
            System.out.println("\tдобавление лемм + индексов  - " + finish1);
            //=======TESTING END===========

            long finish = System.currentTimeMillis() - start;
            System.out.println("\t\tдлительность операции с леммами - " + finish + " мс.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
