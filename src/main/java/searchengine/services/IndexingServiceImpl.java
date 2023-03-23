package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingData;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.StatusType;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final SiteRepository siteRepository;

    @Override
    public IndexingResponse startIndexing() {

        List<Site> sitesList = sites.getSites();

        for (int i=0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            IndexingData data = new IndexingData();
            data.setName(site.getName());
            data.setUrl(site.getUrl());

            searchengine.model.Site s1 = new searchengine.model.Site();


            s1.setNameSite(data.getName());
            s1.setUrl(data.getUrl());
            s1.setTime(LocalDateTime.now());
            s1.setText("Ошибка");
            s1.setStatus(StatusType.INDEXING);
            siteRepository.save(s1);

        }
        IndexingResponse response = new IndexingResponse();


        if (sitesList.size() == 4) {
            response.setResult(true);
            return response;
        } else {
            response.setResult(false);
            response.setError("Индексация уже запущена");
            return response;
        }
    }

    @Override
    public IndexingResponse stopIndexing() {
        return null;
    }
}
