package searchengine.services;

import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

public class SiteIndexing extends Thread {

    private Site site;
    private PageRepository pageRepository;
    private SiteRepository siteRepository;

    public SiteIndexing(Site site, PageRepository pageRepository, SiteRepository siteRepository) {
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
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
        System.out.println("Поток <" + Thread.currentThread().getName() + "> индексирует сайт - " + site.getName());

        //запускаем индексацию при помощи fork-join
        RecursiveIndexingTask recursiveIndexingTask = new RecursiveIndexingTask(siteEntity, siteEntity.getUrl(), siteRepository, pageRepository);
        ForkJoinPool fjp = new ForkJoinPool();
        fjp.invoke(recursiveIndexingTask);

        siteEntity.setStatus(StatusType.INDEXED);
        siteRepository.save(siteEntity);
    }
}
