package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.LemmaResponse;
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

@Log4j2
@Service
@RequiredArgsConstructor
public class LemmaFinderServiceIml implements LemmaFinderService {
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    static LuceneMorphology luceneMorph;
    LemmaResponse response = new LemmaResponse();

    @Override
    public LemmaResponse indexingPage(String url) {
        try {
            luceneMorph = new RussianLuceneMorphology();
            HashMap<String, Integer> totalMap;
            String fullHost = new URL(url).getProtocol() + "://" +  new URL(url).getHost() + "/";
            String link = new JsoupConnect(url).getSimplePath();
            SiteEntity siteEntity = siteRepository.findByUrl(fullHost);
            LemmaEntity lemmaEntity;
            IndexEntity indexEntity;
            if (siteEntity != null) {
                log.info("Find find Entity by url:" + link);
                PageEntity pageEntity = pageRepository.findByPath(link);
                boolean lemmaRepeat = false;
                if (pageEntity != null) {
                    lemmaRepeat = true;
                    indexEntity = indexRepository.findByPage(pageEntity.getPageId());
                    if (indexEntity != null) {
                        indexRepository.deleteByPage(indexEntity.getIndexId());
                    }
                }
                pageEntity = new PageEntity();
                pageEntity.setSite(siteEntity);
                pageEntity.setPath(new JsoupConnect(url).getSimplePath());
                pageEntity.setCode(new JsoupConnect(url).getStatusCodeConnecting());
                pageEntity.setContent(new JsoupConnect(url).getContentFromUrl());
                totalMap = calculateLemmas(url);
                for (Map.Entry<String, Integer> pair : totalMap.entrySet()) {
                    lemmaEntity = lemmaRepository.findByLemma(pair.getKey());
                    if (!lemmaRepeat) {
                        if (lemmaEntity != null) {
                            float newValueFrequency = lemmaEntity.getFrequency() + 1;
                            lemmaRepository.updateFrequency(newValueFrequency, lemmaEntity.getLemmaId());
                        } else {
                            lemmaEntity = new LemmaEntity();
                            lemmaEntity.setLemma(pair.getKey());
                            lemmaEntity.setFrequency(1);
                            lemmaEntity.setSite(siteEntity);
                            lemmaRepository.save(lemmaEntity);
                        }
                    }
                    indexEntity = new IndexEntity();
                    indexEntity.setPage(pageEntity);
                    indexEntity.setLemma(lemmaEntity);
                    indexEntity.setRankT(pair.getValue());
                    indexRepository.save(indexEntity);
                }
                pageRepository.save(pageEntity);
                response.setResult(true);
            }else {
                log.warn("Don`t find Entity by url:" + link);
                response.setResult(false);
                response.setError("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public HashMap<String, Integer> calculateLemmas(String link) throws IOException {
        String content = cleaningHTMLContent(link);
        HashMap<String, Integer> totalMap = new HashMap<>();
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
                    if (totalMap.containsKey(normalWord)) {
                        totalMap.put(normalWord, totalMap.get(normalWord) + 1);
                    } else {
                        totalMap.put(normalWord, 1);
                    }
                }
            }
        }
        return totalMap;
    }

    public String cleaningHTMLContent(String link){
        String content = new JsoupConnect(link).getContentFromUrl();
        return content.toLowerCase(Locale.ROOT)
                .replaceAll("[^а-я]", " ")
                .replaceAll("\\s+", " ");
    }

}
