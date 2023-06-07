package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchErrorResponse extends SearchTotalResponse {

    private String error;

    public SearchErrorResponse(String error) {
        setResult(false);
        this.error = error;
    }

}
