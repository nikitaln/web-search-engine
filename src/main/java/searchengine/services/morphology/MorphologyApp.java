package searchengine.services.morphology;


public class MorphologyApp {

    public static void main(String[] args) {

        String text = "повторное появление леопарда в осетии позволяет предположить";

        MorphologyService morphologyService = new MorphologyService();
        morphologyService.getLemma(text);
    }
}
