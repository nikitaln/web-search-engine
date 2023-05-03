package searchengine.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorURL {
    private final String HTTP_REGEX_RU = "http://www.[^*?!@#$%&()+-,|]+.ru/";
    private final String HTTPS_REGEX_RU = "https://www.[^*?!@#$%&()+-,|]+.ru/";
    private final String HTTP_REGEX_COM = "http://www.[^*?!@#$%&()+-,|]+.com/";

    private final String HTTPS_REGEX_СOM = "https://www.[^*?!@#$%&()+-,|]+.com/";
    private final String HTTP_REGEX_LIKE = "http://www.[^*?!@#$%&()+-,|]+.life/";
    private final String HTTPS_REGEX_LIKE = "https://www.[^*?!@#$%&()+-,|]+.life/";
    private String url;


    public String getSiteURL(String url) {

        String siteURL = "";

        String[] array = {HTTP_REGEX_RU, HTTPS_REGEX_RU,
                HTTP_REGEX_COM,HTTPS_REGEX_СOM,
                HTTP_REGEX_LIKE, HTTPS_REGEX_LIKE};

        for (int i = 0; i < array.length; i++) {

            Pattern pattern = Pattern.compile(array[i]);

            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                siteURL = url.substring(start, end);
            }
        }
        return siteURL;
    }

    public void deleteSite(String url) {
        String site = "https://www.playback.ru/";
        String newUrl = url.substring(site.length()-1);
        System.out.println(newUrl);
    }
}
