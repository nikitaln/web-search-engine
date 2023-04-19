package searchengine.services;

import searchengine.dto.search.SearchTotalResponse;

public interface SearchService {
    SearchTotalResponse searchInformation(String query);
}
