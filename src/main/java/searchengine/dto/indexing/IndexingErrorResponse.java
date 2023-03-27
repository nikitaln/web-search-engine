package searchengine.dto.indexing;

import lombok.Data;

@Data
public class IndexingErrorResponse extends IndexingResponse {

    private String error;

    public IndexingErrorResponse(String error) {
        setResult(false);
        this.error = error;
    }
}
