package searchengine.services.indexing;

import searchengine.dto.indexing.LemmaResponse;

public interface LemmaFinderService {
    LemmaResponse indexingPage(String url);
}
