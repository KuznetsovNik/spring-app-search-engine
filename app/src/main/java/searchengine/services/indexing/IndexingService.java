package searchengine.services.indexing;

import searchengine.dto.appResponse.AppResponse;

public interface IndexingService {

    AppResponse startIndexing();
    AppResponse stopIndexing();

}
