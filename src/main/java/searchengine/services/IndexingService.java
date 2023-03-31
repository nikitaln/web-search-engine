package searchengine.services;

import org.springframework.data.jpa.repository.Query;
import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {

    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();

}
