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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PageIndexing {
    private String url;
    private SiteEntity siteEntity;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    public PageIndexing(String url, SiteEntity siteEntity, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.url = url;
        this.siteEntity = siteEntity;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    public void indexPage() {

        try {
            Document doc2 = Jsoup.connect(url).get();

            PageEntity pageEntity = new PageEntity();

            int statusCode = doc2.connection().response().statusCode();

            if (statusCode < 400) {
                pageEntity.setSite(siteEntity);
                pageEntity.setCodeHTTP(doc2.connection().response().statusCode());
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

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());

            Map<String, Integer> map = new HashMap<>();

            map = lemmaFinder.getLemmaMapWithoutParticles(lemmaFinder.deleteHtmlTags(codeHTML));


            for (String key : map.keySet()) {

                if (lemmaContainsOnDB(key)) {

                    LemmaEntity lemmaEntity = lemmaRepository.findById(lemmaRepository.getLemmaId(key)).get();
                    lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
                    lemmaRepository.save(lemmaEntity);
                    saveIndex(map.get(key), lemmaEntity, pageEntity);

                } else {

                    LemmaEntity lemmaEntity = new LemmaEntity();
                    lemmaEntity.setLemma(key);
                    lemmaEntity.setFrequency(1);
                    lemmaEntity.setSiteEntity(siteEntity);
                    lemmaRepository.save(lemmaEntity);
                    saveIndex(map.get(key), lemmaEntity, pageEntity);

                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean lemmaContainsOnDB(String word) {
        if (word.equals(lemmaRepository.contains(word))) {
            return true;
        } else {
            return false;
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
