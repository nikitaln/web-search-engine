package searchengine.services.morphology;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MorphologyService {

    public void getLemma(String text) {

        List<String> allString = new ArrayList<>();

        String regex = "[.,-]+";

        text = text.replaceAll(regex, "");

        String[] words = text.split("\s");

        for(int i = 0; i < words.length; i++) {

            LuceneMorphology luceneMorphology = null;
            try {
                luceneMorphology = new RussianLuceneMorphology();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<String> wordBaseForms = luceneMorphology.getNormalForms(words[i]);
            allString.addAll(wordBaseForms);
        }

        for (String word : allString) {
            System.out.println(word);
        }
    }
}
