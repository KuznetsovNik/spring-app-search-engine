package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.LemmaResponse;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.JsoupConnect;
import searchengine.services.LinksRecursiveTasking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

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
    private final SitesList sitesList;
    private ForkJoinPool fjp = new ForkJoinPool();
    private boolean firstStart = true;
    private boolean indexing;
    private IndexingResponse response = new IndexingResponse();
    private LemmaResponse lemmaResponse = new LemmaResponse();
    private final LemmaFinderService lemmaFinderService;
    @Override
    public IndexingResponse startIndexing() {
        if (!indexing) {
            indexing = true;
            indexRepository.deleteAll();
            lemmaRepository.deleteAll();
            pageRepository.deleteAll();
            siteRepository.deleteAll();
            indexRepository.resetIdOnIndexest();
            lemmaRepository.resetIdOnLemmas();
            pageRepository.resetIdOnPage();
            siteRepository.resetIdOnSite();
            List<Site> siteList = sitesList.getSites();
            for (Site site : siteList) {
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setUrl(site.getUrl());
                siteEntity.setName(site.getName());
                siteEntity.setStatus(Status.INDEXING);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
                try {
                    if (!fjp.isShutdown()) {
                        List<String> result = fjp.invoke(new LinksRecursiveTasking(site.getUrl(), firstStart));
                        result.forEach(System.out::println);
                        for (String link : result) {
                            if (fjp.isShutdown()) {
                                siteEntity.setStatus(Status.FAILED);
                                siteEntity.setLastError("Индексация остановлена пользователем");
                                siteRepository.save(siteEntity);
                                if (!String.valueOf(new JsoupConnect(link).getStatusCodeConnecting()).startsWith("4")
                                        || !String.valueOf(new JsoupConnect(link).getStatusCodeConnecting()).startsWith("5")) {
                                    PageEntity pageEntity = pageRepository.findByPath(new JsoupConnect(link).getSimplePath());
                                    if (pageEntity == null) {
                                        siteEntity.setStatusTime(LocalDateTime.now());
                                        lemmaResponse = lemmaFinderService.indexingPage(link);
                                    }
                                }
                                break;
                            }else{
                                if (!String.valueOf(new JsoupConnect(link).getStatusCodeConnecting()).startsWith("4")
                                        || !String.valueOf(new JsoupConnect(link).getStatusCodeConnecting()).startsWith("5")) {
                                    PageEntity pageEntity = pageRepository.findByPath(new JsoupConnect(link).getSimplePath());
                                    if (pageEntity == null) {
                                        siteEntity.setStatusTime(LocalDateTime.now());
                                        lemmaResponse = lemmaFinderService.indexingPage(link);
                                    }
                                }
                            }
                        }
                        if (!fjp.isShutdown()) {
                            response.setResult(true);
                            siteEntity.setStatus(Status.INDEXED);
                            siteRepository.save(siteEntity);
                        }
                    } else {
                        siteEntity.setStatus(Status.FAILED);
                        siteEntity.setLastError("Индексация остановлена пользователем");
                        siteRepository.save(siteEntity);
                    }
                } catch (Exception e) {
                    siteEntity.setStatus(Status.FAILED);
                    siteEntity.setLastError(e.getMessage());
                    siteRepository.save(siteEntity);
                    e.printStackTrace();
                }
            }
            firstStart = false;
            indexing = false;
            return response;
        }
        response.setResult(false);
        response.setError("Индексация уже запущена");
        return response;
    }

    @Override
    public IndexingResponse stopIndexing(){
        if (fjp.getPoolSize() == 0){
            response.setResult(false);
            response.setError("Индексация не запущена");
            return response;
        }
        fjp.shutdownNow();
        response.setResult(true);
        return response;
    }
}
