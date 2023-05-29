package searchengine.services.sitemap;

import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

public class SiteMapThread implements Runnable {

    private Site site;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private ForkJoinPool fjp;
    private FlagStop flagStop;
    private RecursiveIndexingTask recursiveIndexingTask;
    private LemmaStorage lemmaStorage = new LemmaStorage();

    public SiteMapThread(Site site, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, FlagStop flagStop) {
        this.site = site;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.flagStop = flagStop;
    }

    @Override
    public void run() {

        SiteEntity siteEntity = createSiteEntity(site.getName(), site.getUrl());
        siteRepository.save(siteEntity);


        System.out.println("site: " + siteEntity.getNameSite() + " -> DB");
        System.out.println("thread: " + Thread.currentThread().getName() + " | indexing site: " + siteEntity.getNameSite());

        //запускаем индексацию при помощи fork-join
        recursiveIndexingTask = new RecursiveIndexingTask(site.getUrl(), siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, flagStop, lemmaStorage);

        fjp = new ForkJoinPool(4);
        fjp.invoke(recursiveIndexingTask);
        fjp.shutdown();

        if (siteEntity.getStatus().equals(StatusType.INDEXING)) {
            siteEntity.setStatus(StatusType.INDEXED);
            siteRepository.save(siteEntity);
        }
        System.out.println("The end :)");
    }

    private SiteEntity createSiteEntity(String siteName, String siteUrl) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setNameSite(siteName);
        siteEntity.setUrl(siteUrl);
        siteEntity.setTime(LocalDateTime.now());
        siteEntity.setText(null);
        siteEntity.setStatus(StatusType.INDEXING);
        return siteEntity;
    }

}
