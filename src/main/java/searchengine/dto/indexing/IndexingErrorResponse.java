package searchengine.dto.indexing;

import lombok.Data;

@Data
public class IndexingErrorResponse extends IndexingResponse {
    /**
     * формирование ответа в случаи ошибки индексации сайта
     * или отдельной страницы
     */

    private String error;
    public IndexingErrorResponse(String error) {
        setResult(false);
        this.error = error;
    }
}
