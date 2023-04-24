package searchengine.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchTotalResponse {
    private boolean results;
    private int count;
    private List<SearchDataResponse> data;
}
