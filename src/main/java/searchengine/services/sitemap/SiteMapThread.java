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
    RecursiveIndexingTask recursiveIndexingTask;
    private Storage storage = new Storage();

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



        //создали сущность site для вставки в БД
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setNameSite(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setTime(LocalDateTime.now());
        siteEntity.setText(null);
        siteEntity.setStatus(StatusType.INDEXING);

        //записали сущность в БД
        siteRepository.save(siteEntity);

        System.out.println("Создали сущность сайт");

        System.out.println("Поток <" + Thread.currentThread().getName() + "> индексирует сайт - " + site.getName());

        //запускаем индексацию при помощи fork-join
        recursiveIndexingTask = new RecursiveIndexingTask(siteEntity.getUrl(), siteEntity, siteRepository, pageRepository, lemmaRepository, indexRepository, flagStop, storage);

        fjp = new ForkJoinPool();
        fjp.invoke(recursiveIndexingTask);
        fjp.shutdown();

        System.out.println("Конец");
    }
}
