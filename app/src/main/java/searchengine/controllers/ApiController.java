package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.LemmaResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.indexing.LemmaFinderService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final LemmaFinderService lemmaFinderService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, LemmaFinderService lemmaFinderService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.lemmaFinderService = lemmaFinderService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing(){
        IndexingResponse response = indexingService.startIndexing();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing(){
        try{
            IndexingResponse response = indexingService.stopIndexing();
                return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> search(@RequestParam @Nullable String query, @RequestParam @Nullable String site,
                                    @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "20") int limit){
        try{
            SearchResponse response = searchService.search(query,site,offset,limit);
            if (response.isResult()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            SearchResponse response = new SearchResponse();
            response.setResult(false);
            response.setError("Указанная страница не найдена");
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam String url) {
        try{
            LemmaResponse response = lemmaFinderService.indexingPage(url);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
