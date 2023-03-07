package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URL;

public class JsoupConnect {
    String url;
    public JsoupConnect(String url) {
        this.url = url;
    }
    public Document connectUrl(){
        Document document = null;
        try {
            Thread.sleep(100);
            document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .timeout(0)
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }
    public String getContentFromUrl() {
        String content = "";
        Document document = connectUrl();
        content = document.outerHtml();
        return content;
    }

    public int getStatusCodeConnecting(){
        int code;
        Document document = connectUrl();
        code = document.connection().response().statusCode();
        return code;
    }

    public String getSimplePath() throws MalformedURLException {
        return new URL(url).getPath();
    }
}
