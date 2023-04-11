package searchengine.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.appResponse.FalseResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingOnePageService;
import searchengine.services.indexing.IndexingService;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

@Log4j2
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final IndexingOnePageService indexingOnePageService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing(){
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing(){
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping(value = "/indexPage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> indexPage(@RequestParam String url){
        try{
            return new ResponseEntity<>(indexingOnePageService.indexingOnePage(url), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex);
            return new ResponseEntity<>(new FalseResponse(false,
                    "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле"),
                    HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam @NonNull String query, @RequestParam @Nullable String site,
                                    @RequestParam(defaultValue = "0") int offset,
                                    @RequestParam(defaultValue = "20") int limit){
        try{
            return new ResponseEntity<>(searchService.search(query,site,offset,limit), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex);
            return new ResponseEntity<>(new FalseResponse(false,
                    "Указанная страница не найдена"),
                    HttpStatus.NOT_FOUND);
        }
    }
}
