package searchengine.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorURL {
    private final String HTTP_REGEX_RU = "http://[^*?!@#$%&()+-,|]+.ru/";
    private final String HTTPS_REGEX_RU = "https://[^*?!@#$%&()+-,|]+.ru/";
    private final String HTTP_REGEX_COM = "http://[^*?!@#$%&()+-,|]+.com/";

    private final String HTTPS_REGEX_СOM = "https://[^*?!@#$%&()+-,|]+.com/";
    private final String HTTP_REGEX_LIKE = "http://[^*?!@#$%&()+-,|]+.life/";
    private final String HTTPS_REGEX_LIKE = "https://[^*?!@#$%&()+-,|]+.life/";
    private String url;
    public EditorURL(String url) {
        this.url = url;
    }

    public String getSiteURL() {

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
}
