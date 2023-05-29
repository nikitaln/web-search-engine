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

import javax.persistence.criteria.CriteriaBuilder;
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

    @Override
    public SearchTotalResponse searchOnAllSites(String query) {

        System.out.println("search for all sites");

        List<SearchDataResponse> listDataResponse = new ArrayList<>();

        //найти все ИД сайтов
        List<Integer> allSitesId = new ArrayList<>();

        Iterator<SiteEntity> iterator = siteRepository.findAll().iterator();

        while (iterator.hasNext()) {
            SiteEntity siteEntity = iterator.next();

            if (siteContainsAllLemmas(siteEntity.getId(), query) == true) {
                allSitesId.add(siteEntity.getId());
            }
        }

        if (allSitesId.isEmpty()) {
            return new SearchErrorResponse("Ничего не найдено по запросу <b>" + query + "<b>");
        }

        for (Integer site : allSitesId) {
            System.out.println("сайт где есть все леммы " + site);
        }


        //ищем все леммы по первому сайту
        for (int i = 0; i < allSitesId.size(); i++) {
            int siteId = allSitesId.get(i);
            Map<String, Integer> map1 = getMapLemmaFrequencyForSite(query, siteId);
            map1 = ascendingSortByValue(map1);
            List<Integer> listPagesId = getPagesWithAllLemmasOnOneSite(map1, siteId);

            int j = 1;
            for (Integer pageId : listPagesId) {
                System.out.println(j + ". Страницы на которых есть все леммы: page ID: " + pageId);
                j = j + 1;
            }
            Map<Integer, Float> mapPagesRelevance = countRank(listPagesId, map1, siteId);
            List<SearchDataResponse> list = getListSearchDataResponse(mapPagesRelevance, query);
            listDataResponse.addAll(list);
        }

        SearchTotalResponse searchTotalResponse = getTotalResponse(listDataResponse);

        return searchTotalResponse;
    }

    @Override
    public SearchTotalResponse searchOnOneSite(String site, String query) {

        int siteId = siteRepository.getId(site);

        if (siteContainsAllLemmas(siteId, query)) {
            Map<String, Integer> map = getMapLemmaFrequencyForSite(query, siteId);
            //сортировка по частоте от редкого слова до популярного
            Map<String, Integer> mapSort = ascendingSortByValue(map);
            //создать список страниц, на которых есть все леммы из запроса
            List<Integer> listPagesId = getPagesWithAllLemmasOnOneSite(mapSort, siteId);

            int i = 1;
            for (Integer pageId : listPagesId) {
                System.out.println(i + ". Страницы на которых есть все леммы: page ID: " + pageId);
                i = i + 1;
            }
            Map<Integer, Float> mapPagesRelevance = countRank(listPagesId, mapSort, siteId);

            List<SearchDataResponse> list = getListSearchDataResponse(mapPagesRelevance, query);
            SearchTotalResponse searchTotalResponse = getTotalResponse(list);

            return searchTotalResponse;
        } else {
            return new SearchErrorResponse("Ничего не найдено по запросу <b>" + query + "<b>");
        }
    }


    private List<Integer> getPagesWithAllLemmasOnOneSite(Map<String, Integer> map, int siteId) {
        List<Integer> pagesId = new ArrayList<>();

        for (String key : map.keySet()) {

            System.out.println("лемма: " + key + " | частота: " + map.get(key));
            int lemmaId = lemmaRepository.getLemmaIdOnSiteId(key, siteId);
            //добавляем ид страниц в коллекцию
            if (pagesId.size() == 0) {
                //все страницы на которых встречается данная лемма
                pagesId = getAllPagesId(lemmaId);
            } else {
                //сверяем список страниц самой редкой леммой, со списком страниц следующей леммы
                List<Integer> list = searchForTheSamePages(pagesId, getAllPagesId(lemmaId));
                pagesId.clear();
                pagesId.addAll(list);
            }
        }
        return pagesId;
    }

    private SearchTotalResponse getTotalResponse(List<SearchDataResponse> list) {
        SearchTotalResponse searchTotalResponse = new SearchTotalResponse();
        searchTotalResponse.setCount(list.size());
        searchTotalResponse.setResult(true);
        searchTotalResponse.setData(list);
        return searchTotalResponse;
    }

    private List<SearchDataResponse> getListSearchDataResponse(Map<Integer, Float> map, String query) {

        List<SearchDataResponse> list = new ArrayList<>();
        for (Integer key : map.keySet()) {

            PageEntity pageEntity = pageRepository.findById(key).get();

            SearchDataResponse data = new SearchDataResponse();

            data.setRelevance(map.get(key));
            data.setUri(pageEntity.getPath());
            data.setSnippet(searchSnippet(pageEntity.getContent(), query));
            data.setTitle(getTitleFromHtmlCode(pageEntity.getContent()));
            data.setSite(pageEntity.getSite().getUrl().substring(0, pageEntity.getSite().getUrl().length()-1));
            data.setSiteName(pageEntity.getSite().getNameSite());

            list.add(data);
        }

        return list;
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

    private List<Integer> searchForTheSamePages(List<Integer> listOld, List<Integer> listNew) {

        List<Integer> list1 = new ArrayList<>();

        for (int i = 0; i < listOld.size(); i++) {
            if (listNew.contains(listOld.get(i))) {
                list1.add(listOld.get(i));
            }
        }
        return list1;
    }

    private Map<Integer, Float> countRank(List<Integer> list, Map<String, Integer> map, int siteId) {

        float maxAbs = 0;
        //id страницы и абсолютная релевантность
        Map<Integer, Float> mapRankAbs = new HashMap<>();
        Map<Integer, Float> mapRankRel = new HashMap<>();


        for (int i = 0; i < list.size(); i++) {
            float abs = 0;
            //c каждой страницы брать количество лемм
            for (String key : map.keySet()) {
                int lemmaId = lemmaRepository.getLemmaIdOnSiteId(key, siteId);
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

        Map<String, Integer> mapRareLemmas = new HashMap<>();

        //удаление слишком популярных лемм, например которые встречаются на всех страницах
        //получаем кол-во страниц
        int countPages = pageRepository.getCount();

        for (String key : map.keySet()) {

            if (map.get(key) < countPages) {
                mapRareLemmas.put(key, map.get(key));
            }
        }
        return mapRareLemmas;
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

        String regex = "[^.,А-Яа-я\\s]";
        String htmlWithoutTags = html.replaceAll(regex, "");

        StringBuilder stringBuilder = new StringBuilder();

        //поиск целого запроса на данной странице
        Pattern pattern = Pattern.compile(query);
        Matcher matcher = pattern.matcher(htmlWithoutTags);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            //System.out.println(htmlWithoutTags.substring(start - 100, end + 100));
            stringBuilder.append("<b>");
            stringBuilder.append(htmlWithoutTags.substring(start, end) + "</b> ");
            stringBuilder.append(htmlWithoutTags.substring(end + 1, end + 200));
            stringBuilder.append("...");
        }



//        for (int i = 0; i < arrayWords.length; i++) {
//            String wordQuery = arrayWords[i];
//            Pattern pattern = Pattern.compile(wordQuery);
//            Matcher matcher = pattern.matcher(htmlWithoutTags);
//            while (matcher.find()) {
//                int start = matcher.start();
//                int end = matcher.end();
//                //System.out.println(htmlWithoutTags.substring(start - 100, end + 100));
//                stringBuilder.append(htmlWithoutTags.substring(start - 50, start - 1) + "<b> ");
//                stringBuilder.append(htmlWithoutTags.substring(start, end) + "</b> ");
//                stringBuilder.append(htmlWithoutTags.substring(end + 1, end + 5));
//            }
//        }

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

    private Map<String, Integer> getMapLemmaFrequencyForSite(String query, int siteId) {
        Map<String, Integer> mapLemmaFrequency = new HashMap<>();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());
            Map<String, Integer> map = lemmaFinder.getLemmaMapWithoutParticles(query);

            for (String key : map.keySet()) {
                //создаем коллекцию ключ-слово, значение-частота
                mapLemmaFrequency.put(key, lemmaRepository.getFrequencyByLemmaAndSite(key, siteId));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapLemmaFrequency;

    }

    private boolean siteContainsAllLemmas(Integer siteId, String query) {

        Map<String, Integer> mapLemmaFrequency = new HashMap<>();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());
            Map<String, Integer> map = lemmaFinder.getLemmaMapWithoutParticles(query);
            int size = map.size();
            int countWord = 0;

            for (String key : map.keySet()) {
                //создаем коллекцию ключ-слово, значение-частота
                //mapLemmaFrequency.put(key, lemmaRepository.getFrequencyByLemmaAndSite(key, siteId));

                if (key.equals(lemmaRepository.getLemmaByLemmaAndSite(key, siteId))) {
                    countWord = countWord + 1;
                }
            }

            if (countWord == size) {
                System.out.println("все слова есть");
                return true;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
