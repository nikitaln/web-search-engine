package searchengine.services.utils;

import com.vdurmont.emoji.EmojiParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorURL {


    public String getSiteURL(String url) {

        String siteURL = "";
        int n = 0;

        for (int i=0; i < url.length(); i++) {
            if (n==3) {
                siteURL = url.substring(0, i);
                break;
            }
            char symbol = url.charAt(i);
            if (symbol == '/') {
                n=n+1;
            }
        }
        return siteURL;
    }


    public void deleteSite(String url) {
        String site = "https://www.playback.ru/";
        String newUrl = url.substring(site.length()-1);
        System.out.println(newUrl);
    }


    public void getTitleFromHtmlCode(String html) {

        System.out.println("Зашли в метод");

        String regex = "<title>([^<>]+)</title>";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String citation = matcher.group(1);
            System.out.println(citation);
        }
    }


    public void containsSite(String url) {

        String[] array = new String[2];
        array[0] = "https://dombulgakova1.ru/";
        array[1] = "https://dombulgakova.ru/";

        for (int i = 0; i < array.length; i++) {

            Pattern pattern = Pattern.compile(array[i]);
            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                System.out.println(url.substring(start, end));
            }
        }
    }


    public String removeEmojiFromText(String text) {

        //String regex = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
        //System.out.println(text.replaceAll(regex, text));

        String textWithoutEmoji = EmojiParser.removeAllEmojis(text);
        return textWithoutEmoji;

    }
}
