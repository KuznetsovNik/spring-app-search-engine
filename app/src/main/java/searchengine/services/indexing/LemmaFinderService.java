package searchengine.services.indexing;

import searchengine.dto.appResponse.AppResponse;
import java.io.IOException;

public interface LemmaFinderService {

    AppResponse indexingPage(String url) throws IOException, InterruptedException;
}
