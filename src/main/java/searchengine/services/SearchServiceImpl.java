package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.LemmaConfiguration;
import searchengine.dto.search.SearchTotalResponse;
import searchengine.model.LemmaEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.lemma.LemmaFinder;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private List<Integer> pagesId = new ArrayList<>();
    private int control = 0;

    @Override
    public SearchTotalResponse searchInformation(String query) {
        control = 0;
        deletePages();

        Map<String, Integer> mapLemmaFrequency = new HashMap<>();
        //получаем список лемм-частота
        mapLemmaFrequency = getMapLemmaFrequency(query);

        //удаление популярный лемм
        mapLemmaFrequency = deletePopularLemma(mapLemmaFrequency);

        //необходимо отсортировать
        mapLemmaFrequency = ascendingSortByValue(mapLemmaFrequency);

        for (String key : mapLemmaFrequency.keySet()) {

            System.out.println("слово: " + key + ", ч-та: " + mapLemmaFrequency.get(key));

            int lemmaId = lemmaRepository.getLemmaId(key);

            if (control == 0) {
                pagesId = getAllPagesId(lemmaId);
                control = control + 1;
            } else {
                searchForTheSamePages(getAllPagesId(lemmaId));
                control = control + 1;
            }
        }

        //поиск всех страниц где встречается лемма

        int i = 1;
        for (Integer pageId : pagesId) {
            System.out.println(i + ". Страницы на которых есть все леммы: " + pageId);
            i = i + 1;
        }

        countRank(pagesId, mapLemmaFrequency);

        return null;
    }

    //получение списка уникальных лемм и их частоты
    private Map<String, Integer> getMapLemmaFrequency(String query) {

        Map<String, Integer> mapLemmaFrequency = new HashMap<>();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());
            Map<String, Integer> map = new HashMap<>();
            map = lemmaFinder.getLemmaMapWithoutParticles(query);

            for (String key : map.keySet()) {
                //создаем коллекцию ключ-слово, значение-частота
                mapLemmaFrequency.put(key, lemmaRepository.getFrequencyByLemma(key));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapLemmaFrequency;
    }

    //сортировка по возрастанию значения частоты
    private Map<String, Integer> ascendingSortByValue(Map<String, Integer> map) {

        map = map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        return map;
    }

    //получение всех Id страниц на которых содержится данная лемма
    private List<Integer> getAllPagesId(int LemmaId) {

        List<Integer> listPagesId = new ArrayList<>();
        listPagesId = indexRepository.getAllPagesIdByLemmaId(LemmaId);

        return listPagesId;
    }

    private void searchForTheSamePages(List<Integer> list) {

        for (int i = 0; i < pagesId.size(); i++) {
            if (!list.contains(pagesId.get(i))) {
                pagesId.remove(i);
                i--;
            }
        }
    }

    private List<Integer> deletePages() {

        for (int i = 0; i < pagesId.size(); i++) {
            pagesId.remove(i);
        }
        return pagesId;
    }

    private void countRank(List<Integer> list, Map<String, Integer> map) {

        float maxAbs = 0;
        //id страницы и абсолютная релевантность
        Map<Integer, Float> mapRankAbs = new HashMap<>();
        Map<Integer, Float> mapRankRel = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {
            System.out.println("ID страницы - " + list.get(i));
            float abs = 0;
            //c каждой страницы брать количество лемм
            for (String key : map.keySet()) {
                int lemmaId = lemmaRepository.getLemmaId(key);
                float rank = indexRepository.getRankByLemmaIdAndPageId(lemmaId, list.get(i));
                System.out.println(key + " rank (" + rank + ")" );
                abs = abs + rank;
            }

            mapRankAbs.put(list.get(i), abs);
            abs = 0;
        }

        for (Integer key : mapRankAbs.keySet()) {
            System.out.println("pageID=" + key + " rank_abs=" + mapRankAbs.get(key));
        }

        //найти максимальное значение rank
        maxAbs = maxAbs + Collections.max(mapRankAbs.values());
        System.out.println("MAX abs = " + maxAbs);

        //заполняем коллекцию относительной релевантности
        for (Integer key : mapRankAbs.keySet()) {
            System.out.println("ранк абс-" + mapRankAbs.get(key) + " max-" + maxAbs);
            float rel = (mapRankAbs.get(key) / maxAbs);
            System.out.println("рел = " + rel);
            mapRankRel.put(key, rel);
        }

        System.out.println();

        for (Integer key : mapRankRel.keySet()) {
            System.out.println("pageID=" + key + " rank_rel=" + mapRankRel.get(key));
        }
    }

    private Map<String, Integer> deletePopularLemma(Map<String, Integer> map) {
        Map<String, Integer> newMap = new HashMap<>();
        for (String key : map.keySet()) {
            if (map.get(key) < 200) {
                newMap.put(key, map.get(key));
            }
        }
        return newMap;
    }
}
