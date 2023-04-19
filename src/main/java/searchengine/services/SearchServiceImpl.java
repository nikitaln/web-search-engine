package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.LemmaConfiguration;
import searchengine.dto.search.SearchTotalResponse;
import searchengine.model.LemmaEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.services.lemma.LemmaFinder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaRepository lemmaRepository;

    //коллекция с частотой встречаемости слов
    private Map<String, Integer> mapLemmaFrequency = new HashMap<>();
    @Override
    public SearchTotalResponse searchInformation(String query) {

        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new LemmaConfiguration().luceneMorphology());
            Map<String, Integer> map = new HashMap<>();
            map = lemmaFinder.getLemmaMapWithoutParticles(query);

            for (String key : map.keySet()) {
                //создаем коллекцию ключ-слово, значение-частота
                mapLemmaFrequency.put(key, lemmaRepository.getFrequencyByLemma(key));
            }

            mapLemmaFrequency.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue())
                    .forEach(System.out::println);

            for (String key : mapLemmaFrequency.keySet()) {
                System.out.println("слово: " + key + ", ч-та: " + mapLemmaFrequency.get(key));
            }

            //продолжение

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
