package searchengine.services.morphology;

public class MorphologyService {

    public void getLemma(String text) {
        
        String regex = "[.,-]+";

        text = text.replaceAll(regex, "");


        String[] words = text.split("\s");

        for(int i = 0; i < words.length; i++) {
            System.out.println(words[i]);
            if (words[i].isEmpty()) {
            }
        }

    }
}
