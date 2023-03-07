package searchengine.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class LinksRecursiveTasking extends RecursiveTask<List<String>> {
    private String url;
    boolean firstStart;
    public LinksRecursiveTasking(String url, boolean firstStart) {
        this.url = url;
        this.firstStart = firstStart;
    }
    @Override
    protected List<String> compute() {
        List<String> result = new ArrayList<>();
        List<LinksRecursiveTasking> linksTasking = new ArrayList<>();
        List<String> linksList = new ArrayList<>();
        try {
            result.add(url);
            linksList.addAll(SiteParser.parserUtils(url,firstStart));
            for (String thisUrl : linksList){
                linksTasking.add(new LinksRecursiveTasking(thisUrl,firstStart));
            }
            for (LinksRecursiveTasking linksTasker : linksTasking){
                linksTasker.fork();
            }
            for (LinksRecursiveTasking linksTasker : linksTasking) {
                result.addAll(linksTasker.join());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
