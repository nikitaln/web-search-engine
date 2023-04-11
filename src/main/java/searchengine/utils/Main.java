package searchengine.utils;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        String url = "http://dimonvideo.ru/api/page1032";

        EditorURL editorURL = new EditorURL(url);

        System.out.println(editorURL.getSiteURL());
    }
}
