package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.appResponse.AppResponse;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.JsoupConnect;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class IndexingOnePageServiceImpl implements IndexingOnePageService{

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private IndexRepository indexRepository;
    private final LemmaFinderService lemmaFinderService;

    @Override
    public AppResponse indexingOnePage(String url) throws IOException, InterruptedException {
        AppResponse response;
        String fullHost = new URL(url).getProtocol() + "://" +  new URL(url).getHost() + "/";
        String link = new JsoupConnect(url).getSimplePath();
        SiteEntity siteEntity = siteRepository.findByUrl(fullHost);
        log.info("Индексируем страницу : " + link);
        PageEntity pageEntity = pageRepository.findByPathAndSiteId(link, siteEntity.getSiteId());
        if (pageEntity != null) {
            cleanThisPageAndHerLemmasFromDB(pageEntity);
        }
        response = lemmaFinderService.indexingPage(url);
        return response;
    }

    private void cleanThisPageAndHerLemmasFromDB(PageEntity pageEntity){
        List<IndexEntity> indexEntityList = indexRepository.findByPageId(pageEntity.getPageId());
        if (!indexEntityList.isEmpty()) {
            for (IndexEntity indexEntity : indexEntityList){
                indexEntity.getLemma().setFrequency(indexEntity.getLemma().getFrequency() - 1);
            }
            indexRepository.multiDeleteByPage(pageEntity.getPageId());
            pageRepository.deletePageById(pageEntity.getPageId());
        }
    }
}
