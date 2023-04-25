package searchengine.dto.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchTotalResponse {
    private boolean result;
    private int count;
    private List<SearchDataResponse> data;

}
