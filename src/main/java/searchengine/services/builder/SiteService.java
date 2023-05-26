package searchengine.services.builder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;
import searchengine.model.StatusType;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteEntity addSite(String siteName, String siteURL) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setNameSite(siteName);
        siteEntity.setUrl(siteURL);
        siteEntity.setTime(LocalDateTime.now());
        siteEntity.setText(null);
        siteEntity.setStatus(StatusType.INDEXING);
        siteRepository.save(siteEntity);

        return siteEntity;
    }

    public void updateSiteTime(SiteEntity siteEntity) {
        siteEntity.setTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    public void updateSiteStatusOnIndexed(SiteEntity siteEntity) {
        siteEntity.setStatus(StatusType.INDEXED);
        siteRepository.save(siteEntity);
    }

    public void updateSiteStatusOnFailed(SiteEntity siteEntity) {
        siteEntity.setStatus(StatusType.FAILED);
        siteEntity.setText("Остановка индексации пользователем");
        siteRepository.save(siteEntity);
    }

    public void deleteSite(SiteEntity siteEntity) {
        siteRepository.delete(siteEntity);
    }


}
