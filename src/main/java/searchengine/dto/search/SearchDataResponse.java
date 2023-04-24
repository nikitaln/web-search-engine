package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchParametersResponse {

    private String site;
    private String siteName;
    private String url;
    private String title;
    private String snippet;
    private double relevance;

}
