package searchengine.utils;

import searchengine.services.builder.PageIndexing;

public class Main {
    public static void main(String[] args) {

        String url = "http://dimonvideo.ru/api/page1032";

        PageIndexing page = new PageIndexing();
        page.indexPage(url);
    }
}
