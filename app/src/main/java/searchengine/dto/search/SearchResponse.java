package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchResponse {
    private boolean result;
    private String error;
    private int count;
    private SearchData searchData;
}
/**
 * 'result': true,
 * 	'count': 574,
 * 	'data': [
 *         {
 * 			"site": ...
 * 			"siteName": ...
 *          "uri": ...
 * 			"title": ...
 * 			"snippet": ...
 * 			"relevance": ...
 * },
 * ...
 * ]
 */


