package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.stereotype.Service;
import searchengine.config.LemmaConfiguration;
import searchengine.dto.search.SearchDataResponse;
import searchengine.dto.search.SearchErrorResponse;
import searchengine.dto.search.SearchTotalResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.security.KeyStore;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Service - анотация Spring, показывает, что наш класс является сервисом,
 * и выполняет функцию поставщиком услуг
 *
 * @RequiredArgsConstructor - анотация библиотеки Lombok, сокращает шаблонный
 * код и генерирует конструктор из полей final
 *
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private List<Integer> pagesId = new ArrayList<>();
    private List<SearchTotalResponse> totalResponse = new ArrayList<>();
    private int control = 0;

    @Override
    public SearchTotalResponse searchInformation(String query) {
        //очищаем список страниц
        pagesId.clear();

        Map<String, Integer> mapLemmaFrequency = new HashMap<>();
        //получаем список лемм-частота
        mapLemmaFrequency = getMapLemmaFrequency(query);

        for (String key : mapLemmaFrequency.keySet()) {
            System.out.println("лемма: " + key + " | частота: " + mapLemmaFrequency.get(key));
        }

        //удаление популярных лемм
        //mapLemmaFrequency = deletePopularLemma(mapLemmaFrequency);

        //необходимо отсортировать по возрастанию частоты от самой маленькой
        System.out.println("\tсортировка по частоте");
        mapLemmaFrequency = ascendingSortByValue(mapLemmaFrequency);

        for (String key : mapLemmaFrequency.keySet()) {

            System.out.println("лемма: " + key + " | частота: " + mapLemmaFrequency.get(key));

            int lemmaId = lemmaRepository.getLemmaId(key);

            //добавляем ид страниц в коллекцию
            if (pagesId.size() == 0) {
                //все страницы на которых встречается данная лемма
                pagesId = getAllPagesId(lemmaId);
            } else {
                //сверяем список страниц самой редкой леммой, со списком страниц следующей леммы
                searchForTheSamePages(getAllPagesId(lemmaId));
            }
        }

        int i = 1;
        for (Integer pageId : pagesId) {
            System.out.println(i + ". Страницы на которых есть все леммы: page ID: " + pageId);
            i = i + 1;
        }

        //расчитать Ранк
        Map<Integer, Float> mapRel = countRank(pagesId, mapLemmaFrequency);

        List<SearchDataResponse> list = new ArrayList<>();

        for (Integer key : mapRel.keySet()) {
            //System.out.println("page ID: " + key + " | Rank rel: " + mapRel.get(key));

            PageEntity pageEntity = pageRepository.findById(key).get();
            SiteEntity siteEntity = pageEntity.getSite();

            SearchDataResponse data = new SearchDataResponse();
            data.setRelevance(mapRel.get(key));
            data.setUri(pageEntity.getPath());

            data.setSnippet(searchSnippet(pageEntity.getContent(), query));
            data.setTitle(getTitleFromHtmlCode(pageEntity.getContent()));
            data.setSite(pageEntity.getSite().getUrl().substring(0, pageEntity.getSite().getUrl().length()-1));
            data.setSiteName(pageEntity.getSite().getNameSite());

            list.add(data);
        }

        SearchTotalResponse searchTotalResponse = new SearchTotalResponse();
        searchTotalResponse.setCount(list.size());
        searchTotalResponse.setResult(true);
        searchTotalResponse.setData(list);

        return searchTotalResponse;
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

    private Map<Integer, Float> countRank(List<Integer> list, Map<String, Integer> map) {

        float maxAbs = 0;
        //id страницы и абсолютная релевантность
        Map<Integer, Float> mapRankAbs = new HashMap<>();
        Map<Integer, Float> mapRankRel = new HashMap<>();


        for (int i = 0; i < list.size(); i++) {
            float abs = 0;
            //c каждой страницы брать количество лемм
            for (String key : map.keySet()) {
                int lemmaId = lemmaRepository.getLemmaId(key);
                float rank = indexRepository.getRankByLemmaIdAndPageId(lemmaId, list.get(i));
                abs = abs + rank;
            }

            mapRankAbs.put(list.get(i), abs);
            abs = 0;
        }

        //найти максимальное значение rank
        maxAbs = maxAbs + Collections.max(mapRankAbs.values());

        //заполняем коллекцию относительной релевантности
        for (Integer key : mapRankAbs.keySet()) {
            float rel = (mapRankAbs.get(key) / maxAbs);
            mapRankRel.put(key, rel);
        }

        //сортировка относительной релевантности
        mapRankRel = descendingSortRelevance(mapRankRel);

        for (Integer key : mapRankRel.keySet()) {
            System.out.println("page ID: " + key + " | Rank rel: " + mapRankRel.get(key));
        }

        return mapRankRel;
    }

    private Map<String, Integer> deletePopularLemma(Map<String, Integer> map) {

        //удаление слишком популярных лемм, например которые встречаются на всех страницах
        //получаем кол-во страниц
        int count = pageRepository.getCount();
        //узнаем кол-во страниц в процентном отношении напрмер 80%
        int maxCountLemma = (count * 80) / 100;


        Map<String, Integer> newMap = new HashMap<>();
        for (String key : map.keySet()) {
            if (map.get(key) < maxCountLemma) {
                newMap.put(key, map.get(key));
            }
        }
        return newMap;
    }

    private Map<Integer, Float> descendingSortRelevance(Map<Integer, Float> map) {

        Map<Integer, Float> result = map.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }

    //получили html код страницы и нужно его пропарсить
    private String searchSnippet(String html, String query) {

        String[] arrayWords = query.split(" ");

        String regex = "[^А-Яа-я\\s]";
        String htmlWithoutTags = html.replaceAll(regex, "");

        StringBuilder stringBuilder = new StringBuilder();


        for (int i = 0; i < arrayWords.length; i++) {
            String wordQuery = arrayWords[i];
            Pattern pattern = Pattern.compile(wordQuery);
            Matcher matcher = pattern.matcher(htmlWithoutTags);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                //System.out.println(htmlWithoutTags.substring(start - 100, end + 100));
                stringBuilder.append(htmlWithoutTags.substring(start - 50, start - 1) + "<b> ");
                stringBuilder.append(htmlWithoutTags.substring(start, end) + "</b> ");
                stringBuilder.append(htmlWithoutTags.substring(end + 1, end + 5));
            }
        }

        return stringBuilder.toString();
    }

    public String getTitleFromHtmlCode(String html) {

        System.out.println("Зашли в метод");

        String regex = "<title>([^<>]+)</title>";
        String citation = "";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            citation = matcher.group(1);
//            System.out.println(citation);
//            return citation;
        }
        return citation;
    }
}
