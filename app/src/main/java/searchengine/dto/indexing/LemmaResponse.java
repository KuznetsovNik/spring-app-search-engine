package searchengine.dto.indexing;

import lombok.Data;

@Data
public class LemmaResponse {
    private boolean result;
    private String error;
}
