package searchengine.services.morphology;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;

public class MorphologyApp {

    public static void main(String[] args) {

        LuceneMorphology luceneMorphology = null;
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> wordBaseForms = luceneMorphology.getNormalForms("леса");

        wordBaseForms.forEach(System.out::println);

        System.out.println("Привет");
        String text = "Повторное появление леопарда в Осетии позволяет предположить,\n" +
                "что леопард постоянно обитает в некоторых районах Северного\n" +
                "Кавказа.";

        MorphologyService morphologyService = new MorphologyService();
        morphologyService.getLemma(text);
    }
}
