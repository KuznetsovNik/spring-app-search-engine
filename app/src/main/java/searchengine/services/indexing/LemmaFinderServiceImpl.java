package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.appResponse.AppResponse;
import searchengine.dto.appResponse.TrueResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.JsoupConnect;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import static searchengine.services.JsoupConnect.getContentFromUrl;
import static searchengine.services.JsoupConnect.getStatusCodeConnecting;

@Log4j2
@Service
@RequiredArgsConstructor
public class LemmaFinderServiceImpl implements LemmaFinderService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;

    @Override
    public AppResponse indexingPage(String url) throws IOException {
        AppResponse response;
        String fullHost = new URL(url).getProtocol() + "://" +  new URL(url).getHost() + "/";
        Document document = new JsoupConnect(url).connectUrl();
        String link = new JsoupConnect(url).getSimplePath();
        SiteEntity siteEntity = siteRepository.findByUrl(fullHost);

        log.info("Индексируем страницу : " + link);

        PageEntity pageEntity = new PageEntity(siteEntity,
                link,
                getStatusCodeConnecting(document),
                getContentFromUrl(document));
        pageRepository.save(pageEntity);

        Map<String, Integer> mapLemmaAndRank = calculateLemmas(pageEntity);
        List<IndexEntity> indexEntityList = new ArrayList<>();
        List<LemmaEntity> lemmaEntityList = new ArrayList<>();
        for (Map.Entry<String, Integer> mapEntryLemmaAndRank : mapLemmaAndRank.entrySet()) {
            LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSiteId(mapEntryLemmaAndRank.getKey(), siteEntity.getSiteId());
            if (lemmaEntity != null) {
                lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
            } else {
                lemmaEntity = new LemmaEntity(mapEntryLemmaAndRank.getKey(), 1, siteEntity);
                lemmaEntityList.add(lemmaEntity);
            }
            IndexEntity indexEntity = new IndexEntity(pageEntity, lemmaEntity, mapEntryLemmaAndRank.getValue());
            indexEntityList.add(indexEntity);
        }
        lemmaRepository.saveAll(lemmaEntityList);
        indexRepository.saveAll(indexEntityList);
        response = new TrueResponse(true);
        return response;
    }

    private HashMap<String, Integer> calculateLemmas(PageEntity page) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        String content = cleaningHTMLContent(page);
        HashMap<String, Integer> mapLemmaAndRank = new HashMap<>();
        String[] words = content.toLowerCase(Locale.ROOT).split("[^а-я]");
        for (String word : words){
            if (!word.isEmpty()) {
                List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
                if (!wordBaseForms.stream().allMatch(e -> e.toUpperCase().contains("СОЮЗ") || e.toUpperCase().contains("МЕЖД") || e.toUpperCase().contains("ПРЕДЛ"))) {
                    List<String> normalForms = luceneMorph.getNormalForms(word);
                    if (normalForms.isEmpty()) {
                        continue;
                    }
                    String normalWord = normalForms.get(0);
                    if (mapLemmaAndRank.containsKey(normalWord)) {
                        mapLemmaAndRank.put(normalWord, mapLemmaAndRank.get(normalWord) + 1);
                    } else {
                        mapLemmaAndRank.put(normalWord, 1);
                    }
                }
            }
        }
        return mapLemmaAndRank;
    }

    public static String cleaningHTMLContent(PageEntity page){
        StringBuilder outputBuilder = new StringBuilder();
        Document jsoupDoc = Jsoup.parse(page.getContent());
        outputBuilder.append(jsoupDoc.body().text());
        return outputBuilder.toString();
    }
}
