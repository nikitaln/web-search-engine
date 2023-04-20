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

    private List<Integer> pages = new ArrayList<>();

    //коллекция с частотой встречаемости слов
//    private Map<String, Integer> mapLemmaFrequency = new HashMap<>();
    @Override
    public SearchTotalResponse searchInformation(String query) {

        deletePages();

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());
            Map<String, Integer> map = new HashMap<>();
            map = lemmaFinder.getLemmaMapWithoutParticles(query);

            Map<String, Integer> mapLemmaFrequency = new HashMap<>();

            for (String key : map.keySet()) {
                //создаем коллекцию ключ-слово, значение-частота
                mapLemmaFrequency.put(key, lemmaRepository.getFrequencyByLemma(key));
            }

            //сортировка по увеличению значения
            mapLemmaFrequency = mapLemmaFrequency.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new
                    ));


            for (String key : mapLemmaFrequency.keySet()) {

                System.out.println("слово: " + key + ", ч-та: " + mapLemmaFrequency.get(key));

                //получить id леммы
                int idLemma = lemmaRepository.getLemmaId(key);

                //получить все id страницы содержащие данную лемму
//                List<Integer> listAllPagesId = getAllPagesId(idLemma);

                if (pages.size() == 0) {
                    //создается главный список id страниц с которым будет сравнение
                    pages.addAll(getAllPagesId(idLemma));
                } else {
                    checkPages(getAllPagesId(idLemma));
                }
            }

            for (Integer page : pages) {
                System.out.println("Осталась страница " + page);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    //получение всех Id страниц на которых содержится данная лемма
    private List<Integer> getAllPagesId(int id) {

        List<Integer> listPagesId = new ArrayList<>();
        listPagesId = indexRepository.getAllPagesIdByLemmaId(id);

        return listPagesId;
    }

    private void checkPages(List<Integer> idPages) {

        for (int i = 0; i < pages.size(); i++) {
            int id = pages.get(i);
            if (!idPages.contains(id)) {
                pages.remove(i);
                i--;
            }
        }
    }

    public void deletePages() {

        for (int i = 0; i < pages.size(); i++) {
            pages.remove(i);
        }
    }
}
