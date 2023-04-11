package searchengine.services.indexing;

import searchengine.dto.appResponse.AppResponse;

import java.io.IOException;

public interface IndexingOnePageService {
    AppResponse indexingOnePage(String url) throws IOException, InterruptedException;
}
