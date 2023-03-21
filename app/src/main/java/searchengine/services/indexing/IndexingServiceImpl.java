package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.appResponse.AppResponse;
import searchengine.dto.appResponse.FalseResponse;
import searchengine.dto.appResponse.TrueResponse;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.JsoupConnect;
import searchengine.services.LinksRecursiveTasking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Log4j2
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private boolean indexing;
    private final SitesList sitesList;
    private ForkJoinPool forkJoinPool = new ForkJoinPool();
    private boolean firstStart = true;
    private final LemmaFinderService lemmaFinderService;

    @Override
    public AppResponse startIndexing() {
        AppResponse response;
        if (indexing) {
            response = new FalseResponse(false, "Индексация уже запущена");
            log.info(response);
            return response;
        }
        indexing = true;

        cleanAndResetTablesDB();

        List<Site> siteList = sitesList.getSites();
        for (Site site : siteList) {
            SiteEntity siteEntity = new SiteEntity(site.getUrl(),site.getName(),Status.INDEXING,LocalDateTime.now());
            siteRepository.save(siteEntity);

            List<String> listLinks = forkJoinPool.invoke(new LinksRecursiveTasking(site.getUrl(), firstStart));
            log.info(listLinks);
            for (String link : listLinks) {
                if (forkJoinPool.isShutdown()){
                    siteEntity.setStatus(Status.FAILED);
                    siteEntity.setLastError("Индексация остановлена пользователем");
                    siteRepository.save(siteEntity);
                    indexing = false;
                    response = new FalseResponse(false, "Индексация остановлена пользователем");
                    log.info(response);
                    return response;
                }
                try {
                    if (isStatusCodeFine(link)) {
                        indexingPage(link, siteEntity);
                    }
                }catch (Exception ex){
                    siteEntity.setStatus(Status.FAILED);
                    siteEntity.setLastError(ex.getMessage());
                    siteRepository.save(siteEntity);
                    log.error(ex);
                }
            }
            siteEntity.setStatus(Status.INDEXED);
            siteRepository.save(siteEntity);
        }
        firstStart = false;
        indexing = false;
        response = new TrueResponse(true);
        return response;
    }

    @Override
    public AppResponse stopIndexing(){
        AppResponse response;
        if (forkJoinPool.getPoolSize() == 0){
            response = new FalseResponse(false, "Индексация не запущена");
            log.warn(response);
            return response;
        }
        forkJoinPool.shutdownNow();
        response = new TrueResponse(true);
        return response;
    }

    private void cleanAndResetTablesDB(){
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
        indexRepository.resetIdOnIndexest();
        lemmaRepository.resetIdOnLemmas();
        pageRepository.resetIdOnPage();
        siteRepository.resetIdOnSite();
    }

    private boolean isStatusCodeFine(String link) throws IOException, InterruptedException {
        return !String.valueOf(new JsoupConnect(link).getStatusCodeConnecting()).startsWith("4")
                    || !String.valueOf(new JsoupConnect(link).getStatusCodeConnecting()).startsWith("5");
    }

    private void indexingPage(String link, SiteEntity siteEntity) throws IOException, InterruptedException {
        PageEntity pageEntity = pageRepository.findByPathAndSiteId(new JsoupConnect(link).getSimplePath(), siteEntity.getSiteId());
        if (pageEntity == null) {
            siteEntity.setStatusTime(LocalDateTime.now());
            lemmaFinderService.indexingPage(link);
        }
    }
}
