package searchengine.config;

public class UserAgent {
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36";
    private final String REFERRER = "http://www.google.com";

    public String getUSER_AGENT() {
        return USER_AGENT;
    }
    public String getREFERRER() {
        return REFERRER;
    }
}
