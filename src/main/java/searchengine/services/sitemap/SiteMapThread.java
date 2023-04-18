package searchengine.services.sitemap;

import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

public class SiteMapThread implements Runnable {

    private Site site;
    private PageRepository pageRepository;
    private SiteRepository siteRepository;
    private ForkJoinPool fjp;
    private FlagStop flagStop;

    RecursiveIndexingTask recursiveIndexingTask;

    public SiteMapThread(Site site, PageRepository pageRepository, SiteRepository siteRepository, FlagStop flagStop) {
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.flagStop = flagStop;
    }

    @Override
    public void run() {

        //создали сущность site для вставки в БД
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setNameSite(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setTime(LocalDateTime.now());
        siteEntity.setText(null);
        siteEntity.setStatus(StatusType.INDEXING);

        //записали сущность в БД
        siteRepository.save(siteEntity);

        System.out.println("Создали сущность - " + siteEntity.getNameSite());

        System.out.println("Поток <" + Thread.currentThread().getName() + "> индексирует сайт - " + site.getName());

        //запускаем индексацию при помощи fork-join
        recursiveIndexingTask = new RecursiveIndexingTask(siteEntity, siteEntity.getUrl(), siteRepository, pageRepository, flagStop);
        fjp = new ForkJoinPool();
        fjp.invoke(recursiveIndexingTask);

        System.out.println("Конец");
    }
}
