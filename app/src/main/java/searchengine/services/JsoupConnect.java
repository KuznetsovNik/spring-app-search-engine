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

    public Document connectUrl() throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (WindowsNT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                .referrer("http://www.google.com")
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .followRedirects(false)
                .timeout(0)
                .get();
        return document;
    }

    public static String getContentFromUrl(Document document) {
        String content = "";
        content = document.outerHtml();
        return content;
    }

    public static int getStatusCodeConnecting(Document document) {
        int code;
        code = document.connection().response().statusCode();
        return code;
    }

    public String getSimplePath() throws MalformedURLException {
        return new URL(url).getPath();
    }
}
