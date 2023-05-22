package searchengine.services.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class MainTest {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        Jsoup.connect("https://dombulgakova.ru/?method=ical&id=5116");




        long finish = System.currentTimeMillis() - start;
        System.out.println("time | " + finish);

    }
}
