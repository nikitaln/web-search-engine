package searchengine.services.sitemap;

import searchengine.model.LemmaEntity;

import java.util.*;

public class LemmaStorage {

    private Map<String, LemmaEntity> mapLemmas = new HashMap<>();

    public void addLemma(String lemma, LemmaEntity lemmaEntity) {
        mapLemmas.put(lemma, lemmaEntity);
    }
    public boolean containsKeyMapLemmas(String lemma) {
        return mapLemmas.containsKey(lemma) ? true : false;
    }
    public LemmaEntity getLemmaEntity(String lemma) {
        return mapLemmas.get(lemma);
    }
    public void clearMapLemmas() {
        mapLemmas.clear();
    }
    public int getSizeMapLemmas() {
        return mapLemmas.size();
    }
}
