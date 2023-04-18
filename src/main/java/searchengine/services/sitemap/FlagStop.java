package searchengine.services.sitemap;

public class FlagObject {
    public volatile boolean stopNow = false;

    protected void stopForkJoin() {
        stopNow = true;
    }
}
