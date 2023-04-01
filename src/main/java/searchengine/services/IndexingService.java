package searchengine.services;

import searchengine.dto.indexing.IndexingErrorResponse;
import searchengine.dto.indexing.IndexingResponse;
public interface IndexingService {

    IndexingResponse startIndexing();
    IndexingErrorResponse stopIndexing();
}
