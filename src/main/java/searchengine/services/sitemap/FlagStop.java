package searchengine.services.sitemap;

public class FlagStop {
    public volatile boolean stopNow = false;

    public boolean isStopNow() {
        return stopNow;
    }

    public void stopRecursiveTask() {
        stopNow = true;
    }
}
