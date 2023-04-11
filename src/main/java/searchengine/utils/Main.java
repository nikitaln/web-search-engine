package searchengine.utils;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {


        try {
            Document doc = Jsoup.connect("playback.ru/catalog/1141.html").get();

            Elements elements = doc.select("a");

            elements.forEach(element -> {
                System.out.println(element.attr("href"));
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
