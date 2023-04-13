package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static searchengine.services.JsoupConnect.getStatusCodeConnecting;

public class SiteParser {
    private static HashSet<String> uniqueUrl = new HashSet<>();

    public static List<String> parserUtils(String url,boolean firstStart) throws IOException, InterruptedException {
        if (!firstStart) {
            uniqueUrl = new HashSet<>();
        }
        String [] hostArray = new URL(url).getAuthority().split("\\.");
        String host = new URL(url).getProtocol() + "://" + hostArray[0];
        List<String> listLinks = new ArrayList<>();
        Thread.sleep(100);
            Document document = new JsoupConnect(url).connectUrl();
            Elements link = document.select("a[href]");
            if (!link.isEmpty()) {
                for (Element element : link) {
                    String thisUrl = element.absUrl("href");
                    synchronized (uniqueUrl) {
                        if (!thisUrl.isEmpty()
                                && !thisUrl.contains("#")
                                && !uniqueUrl.contains(thisUrl)
                                && thisUrl.startsWith(host)
                                && (thisUrl.endsWith("/") || thisUrl.endsWith(".html"))
                        ) {
                            Connection.Response response = Jsoup.connect(thisUrl).execute();
                            if (isStatusCodeFine(response.statusCode())) {
                                uniqueUrl.add(thisUrl);
                                int countSpace = 1;
                                listLinks.add(getSpace(countSpace) + thisUrl);
                            }
                        }
                    }
                }
            }
        return listLinks;
    }
    private static String getSpace(int tubsCount){
        return " " + " ".repeat(Math.max(0, tubsCount));
    }

    private static boolean isStatusCodeFine(int statusCode) {
        return !String.valueOf(statusCode).startsWith("4")
                || !String.valueOf(statusCode).startsWith("5");
    }
}
