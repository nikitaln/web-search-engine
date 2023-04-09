package searchengine.services.lemma;

import org.apache.lucene.morphology.LuceneMorphology;

import java.util.*;

public class LemmaFinder {
    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }




    //Основной метод начало получения леммы слов из текста
    public Map<String, Integer> getLemmaMapWithParticles(String text) {

        String[] words = arrayContainsRussianWords(text);

        Map<String, Integer> lemmas = new HashMap<>();

        //пробегаемся циклом по всем словам
        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            //создаем список всех слов и частиц
            List<String> wordBaseForms = luceneMorphology.getNormalForms(word);

            //проверяем относится ли своло к частице
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else if (isCorrectWordForm(normalWord)){
                lemmas.put(normalWord, 1);
            }
        }

        return lemmas;
        }

    public Map<String, Integer> getLemmaMapWithoutParticles(String text) {

        String[] textArray = arrayContainsRussianWords(text);
        Map<String, Integer> lemmaMap = new HashMap<>();

        for (String word : textArray) {
            if (!word.isEmpty() && isCorrectWordForm(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }

                List<String> normalForms = luceneMorphology.getNormalForms(word);
                if (normalForms.isEmpty()) {
                    continue;
                }

                String normalWord = normalForms.get(0);

                if (lemmaMap.containsKey(normalWord)) {
                    lemmaMap.put(normalWord, lemmaMap.get(normalWord) + 1);
                } else if (isCorrectWordForm(normalWord)) {
                    lemmaMap.put(normalWord, 1);
                }
            }
        }
        return lemmaMap;
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    //проверяем является ли слово - частицей
    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {

            if (wordBase.contains(property)) {
                return true;
            }
        }
        return false;
    }

    //получение массива русских слов из переданного текста
    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isCorrectWordForm(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    public String deleteHtmlTags(String html) {
        String regex = "[^А-Яа-я\\s]";
        String htmlWithoutTags = html.replaceAll(regex, "");
        return htmlWithoutTags;
    }
}
