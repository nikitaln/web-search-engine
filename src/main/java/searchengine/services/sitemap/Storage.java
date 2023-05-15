package searchengine.services.sitemap;

import searchengine.model.LemmaEntity;

import java.util.*;

public class Storage {

    //лемма и частота
    private Map<String, Integer> mapLemmas = new HashMap<>();
    public static Map<String, LemmaEntity> lemmas = new HashMap<>();

    public void addLemma(String word, int count) {
        mapLemmas.put(word, count);
    }

    public void deleteLemma(String word) {
        mapLemmas.remove(word);
    }

    public int getValue(String word) {
        return mapLemmas.get(word);
    }
    public boolean contains(String word) {
        return mapLemmas.containsKey(word);
    }

}
