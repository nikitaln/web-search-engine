package searchengine.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorURL {
    private final String HTTP = "http://";
    private final String HTTPS = "https://";
    private final String WWW = "www.";
    private final String domainRU = "[^*?!@#$%&()+-,|\\].ru";
    private final String domainCOM = ".com";
    private String url;
    public EditorURL(String url) {
        this.url = url;
    }

    public void getSiteURL(String url) {
        Pattern pattern;

        if (isHTTP(url)) {
            System.out.printf("мы в http");

            if (isWWW(url)) {
                System.out.printf("Мы попали в www");

                pattern = Pattern.compile(domainRU);
                Matcher matcher = pattern.matcher(domainRU);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    System.out.println(domainRU.substring(start, end));

                    System.out.printf("Мы попали");
                }



            }


        } else if (isHTTPS()) {

        } else {

        }


    }

    private boolean isHTTP(String url) {
        return url.substring(0, HTTP.length()).equals(HTTP) ? true : false;
    }
    private boolean isHTTPS() {
        return url.substring(0, HTTPS.length()).equals(HTTPS) ? true : false;
    }

    private boolean isWWW(String url) {
    return false;
    }



}
