package searchengine.services.lemma;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MorphologyApp {

    public static void main(String[] args) throws IOException {

        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> wordBaseForms = luceneMorph.getMorphInfo("не");
        wordBaseForms.forEach(System.out::println);

        String text = "Повторное появление леопарда в Осетии позволяет предположить, что и леопард постоянно обитает в некоторых районах Северного Кавказа.";


        LemmaFinder lemmaFinder = new LemmaFinder(luceneMorph);
//        Map<String, Integer> map = new HashMap<>();
//
//        map = lemmaFinder.getLemmaMapWithoutParticles(text);
//
//        for (String key : map.keySet()) {
//            System.out.println(key + " " + map.get(key));
//        }


    }
}
