package searchengine.dto.search;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SearchResponse {
    private boolean result;
    private int count;
    private List<PageDto> data;
}


