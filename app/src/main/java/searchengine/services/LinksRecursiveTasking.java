package searchengine.services;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

@Log4j2
@AllArgsConstructor
public class LinksRecursiveTasking extends RecursiveTask<List<String>> {
    private String url;
    private boolean firstStart;

    @Override
    protected List<String> compute() {
        List<String> listLinks = new ArrayList<>();
        List<LinksRecursiveTasking> linksTasking = new ArrayList<>();
        try {
            listLinks.add(url);
            List<String> linksList = new ArrayList<>(SiteParser.parserUtils(url, firstStart));
            for (String thisUrl : linksList){
                linksTasking.add(new LinksRecursiveTasking(thisUrl,firstStart));
            }
            for (LinksRecursiveTasking linksTasker : linksTasking){
                linksTasker.fork();
            }
            for (LinksRecursiveTasking linksTasker : linksTasking) {
                listLinks.addAll(linksTasker.join());
            }
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        return listLinks;
    }
}
