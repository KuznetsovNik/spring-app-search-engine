package searchengine.services;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@AllArgsConstructor
public class JsoupConnect {
    private String url;

    public Document connectUrl() throws IOException, InterruptedException {

        Thread.sleep(100);

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .followRedirects(false)
                .timeout(0)
                .get();

        return document;
    }
    public String getContentFromUrl() throws IOException, InterruptedException {
        String content = "";
        Document document = connectUrl();
        content = document.outerHtml();
        return content;
    }

    public int getStatusCodeConnecting() throws IOException, InterruptedException {
        int code;
        Document document = connectUrl();
        code = document.connection().response().statusCode();
        return code;
    }

    public String getSimplePath() throws MalformedURLException {
        return new URL(url).getPath();
    }
}
