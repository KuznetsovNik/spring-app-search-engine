package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchData {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;
}
/**
 * "site": "http://www.site.com"
 * "siteName": "Имя сайта"
 * "uri": "/path/to/page/6784"
 * "title": "Заголовок страницы,которую выводим"
 * "snippet": "Фрагмент текста,в котором найдены совпадения, <b>выделенные жирным</b>, в формате HTML"
 * "relevance": 0.93362
 */
