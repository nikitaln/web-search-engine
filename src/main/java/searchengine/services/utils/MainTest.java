package searchengine.services.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainTest {

    public static void main(String[] args) {

        EditorURL url = new EditorURL();
        System.out.println(url.getSiteURL("https://www.playback.ru/catalog/1141.html"));
    }
}
