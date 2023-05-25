package searchengine.services;

import searchengine.dto.search.SearchTotalResponse;

public interface SearchService {
    SearchTotalResponse searchOnAllSites(String query);
    SearchTotalResponse searchOnOneSite(String site, String query);
}
