package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.PageDto;
import searchengine.dto.search.SearchResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    private final int frequencyValue = 50;

    @Override
    public SearchResponse search(String searchQuery, String searchSite, int searchOffset, int searchLimit) throws IOException {
        SearchResponse response = new SearchResponse();
        List<PageDto> pageDtoList = new ArrayList<>();
        log.info("Поисковая строка: " + searchQuery);
        if (searchSite != null) {
            log.info("Ведем поиск по сайту: " + searchSite);
            SiteEntity siteEntity = siteRepository.findByUrl(searchSite);
            if (siteEntity != null) {
                int siteId = siteEntity.getSiteId();
                pageDtoList.addAll(getAllPageDto(siteId, searchQuery,false));
                response.setResult(true);
                response.setCount(pageDtoList.size());
                if (pageDtoList.size() > searchLimit){
                    pageDtoList = pageDtoList.subList(searchOffset,searchLimit);
                }
                response.setData(pageDtoList);
            }
        }else{
            for (int i = 1; i <= siteRepository.count(); i++) {
                log.info("Ведем поиск по сайту: " + siteRepository.findById(i).get().getName());
                pageDtoList.addAll(getAllPageDto(i, searchQuery,true));
            }
            response.setResult(true);
            response.setCount(pageDtoList.size());
            float biggerRelevance = pageDtoList.stream().sorted().findFirst().get().getRelevance();
            pageDtoList.forEach(pageDto -> pageDto.calculateRelToAbsRelevance(biggerRelevance));
            if (pageDtoList.size() > searchLimit){
                pageDtoList = pageDtoList.subList(searchOffset,searchLimit);
            }
            response.setData(pageDtoList.stream().sorted().collect(Collectors.toList()));
        }
        return response;
    }

    private List<PageDto> getAllPageDto(int siteId, String searchQuery, boolean siteMoreOne) throws IOException {
        Map<Integer, String> pageAndLemmaMap = new HashMap<>();
        Set<Integer> setPagesIdHavingTargetLemma = new HashSet<>();
        List<Map.Entry<String, Integer>> listMapEntryLemmasAndTheirFrequency = getListMapEntryLemmasAndTheirFrequency(searchQuery, siteId);
        for (Map.Entry<String, Integer> LemmasAndTheirFrequencyEntry : listMapEntryLemmasAndTheirFrequency) {
            String lemma = LemmasAndTheirFrequencyEntry.getKey();
            int lemmaId = lemmaRepository.findByLemmaAndSiteId(lemma, siteId).getLemmaId();
            List<Integer> listPagesIdHaveLemma = indexRepository.findByLemmaId(lemmaId);
            log.info("Лемма \"" + lemma + "\" встречается на страницах: " + listPagesIdHaveLemma);
            setPagesIdHavingTargetLemma.addAll(listPagesIdHaveLemma);
            for (Integer pageId : listPagesIdHaveLemma) {
                pageAndLemmaMap.put(pageId, lemma);
            }
        }
        if (!siteMoreOne) {
            Map<Integer, Float> mapPageIdAndTotalRelevance = calculationMapPageAbsRelevance(setPagesIdHavingTargetLemma, listMapEntryLemmasAndTheirFrequency, siteId);
            List<Integer> sortedListPagesIdByRelevance = sortingListPagesIdByRelevance(mapPageIdAndTotalRelevance);
            return getMapperPageDto(sortedListPagesIdByRelevance, pageAndLemmaMap, mapPageIdAndTotalRelevance);
        }else{
            Map<Integer, Float> mapPageIdAndRelativeRelevance = calculationMapPageIdAndRelRelevance(setPagesIdHavingTargetLemma, listMapEntryLemmasAndTheirFrequency, siteId);
            List<Integer> sortedListPagesIdByRelevance = sortingListPagesIdByRelevance(mapPageIdAndRelativeRelevance);
            return getMapperPageDto(sortedListPagesIdByRelevance, pageAndLemmaMap, mapPageIdAndRelativeRelevance);
        }
    }

    private List<Map.Entry<String, Integer>> getListMapEntryLemmasAndTheirFrequency(String searchQuery, int siteId) throws IOException {
        Map<String,Integer> mapLemmaAndFrequency = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        String [] queryArray = searchQuery.toLowerCase().split("([^а-яa-z0-9ё]+)");
        for (String word : queryArray) {
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            if (!wordBaseForms.stream().allMatch(e -> e.toUpperCase().contains("СОЮЗ") || e.toUpperCase().contains("МЕЖД") || e.toUpperCase().contains("ПРЕДЛ"))) {
                List<String> normalForms = luceneMorph.getNormalForms(word);
                if (normalForms.isEmpty()) {
                    continue;
                }
                String normalWord = normalForms.get(0);
                LemmaEntity lemma;
                if (siteId == 0) {
                    for (int i = 0; i <= siteRepository.count(); i++) {
                        lemma = lemmaRepository.findByLemmaAndSiteId(normalWord, i);
                        if (lemma != null) {
                            mapLemmaAndFrequency.put(normalWord, lemma.getFrequency());
                        }
                    }
                }else{
                    lemma = lemmaRepository.findByLemmaAndSiteId(normalWord, siteId);
                    if (lemma != null) {
                        mapLemmaAndFrequency.put(normalWord, lemma.getFrequency());
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> listMapEntryLemmasAndTheirFrequency = mapLemmaAndFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        listMapEntryLemmasAndTheirFrequency.removeIf(el-> el.getValue() > frequencyValue);
        return listMapEntryLemmasAndTheirFrequency;
    }



    private Map<Integer, Float> calculationMapPageAbsRelevance(Set<Integer> setPagesIdHavingTargetLemma, List<Map.Entry<String, Integer>> listMapEntryLemmasAndTheirFrequency, int siteId){
        Map<Integer, Float> mapPageIdAndTotalRelevance = new HashMap<>();
        float biggerAbsRel = 0.0f;
        for (Integer pageId : setPagesIdHavingTargetLemma){
            float absoluteRelevance = 0.0f;
            for (Map.Entry<String, Integer> EntryLemmasAndTheirFrequency : listMapEntryLemmasAndTheirFrequency) {
                String lemma = EntryLemmasAndTheirFrequency.getKey();
                int lemmaId = lemmaRepository.findByLemmaAndSiteId(lemma, siteId).getLemmaId();
                IndexEntity indexEntity = indexRepository.findByPageIdAndLemmaId(pageId,lemmaId);
                if (indexEntity != null) {
                    absoluteRelevance += indexEntity.getRankT();
                }
            }
            if (absoluteRelevance > biggerAbsRel){
                biggerAbsRel = absoluteRelevance;
            }
            mapPageIdAndTotalRelevance.put(pageId, absoluteRelevance);
        }
        mapPageIdAndTotalRelevance.forEach((key, value) -> log.info("Страница № " + key + " - " + value + " aбсю релевантноть"));
        float finalBiggerAbsRel = biggerAbsRel;
        mapPageIdAndTotalRelevance.entrySet().forEach(e -> e.setValue(e.getValue()/ finalBiggerAbsRel));
        mapPageIdAndTotalRelevance.forEach((key, value) -> log.info("Страница № " + key + " - " + value + " относ релевантноть"));
        return mapPageIdAndTotalRelevance;
    }

    private List<Integer> sortingListPagesIdByRelevance(Map<Integer, Float> mapPageIdAndTotalRelevance){
        Comparator<Map.Entry<Integer,Float>> comparator = Map.Entry.comparingByValue();
        return mapPageIdAndTotalRelevance.entrySet().stream()
                .sorted(comparator.reversed()).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<PageDto> getMapperPageDto(List<Integer> sortedListPagesIdByRelevance, Map<Integer,String> pageAndLemmaMap, Map<Integer, Float> mapPageIdAndTotalRelevance) {
        List<PageDto> pageDtoList = new ArrayList<>();
        for (Integer pageId : sortedListPagesIdByRelevance){
            PageDto pageDto = PageDto.getPageDtoFromEntity(
                    pageRepository.findPageById(pageId),
                    pageAndLemmaMap.get(pageId),
                    mapPageIdAndTotalRelevance.get(pageId));
            pageDtoList.add(pageDto);
        }
        return pageDtoList;
    }

    private Map<Integer, Float> calculationMapPageIdAndRelRelevance(Set<Integer> setPagesIdHavingTargetLemma, List<Map.Entry<String, Integer>> listMapEntryLemmasAndTheirFrequency, int siteId){
        Map<Integer, Float> mapPageIdRelRelevance = new HashMap<>();
        for (Integer pageId : setPagesIdHavingTargetLemma){
            float relativeRelevance = 0.0f;
            for (Map.Entry<String, Integer> EntryLemmasAndTheirFrequency : listMapEntryLemmasAndTheirFrequency) {
                String lemma = EntryLemmasAndTheirFrequency.getKey();
                int lemmaId = lemmaRepository.findByLemmaAndSiteId(lemma, siteId).getLemmaId();
                IndexEntity indexEntity = indexRepository.findByPageIdAndLemmaId(pageId,lemmaId);
                if (indexEntity != null) {
                    relativeRelevance += indexEntity.getRankT();
                }
            }
            mapPageIdRelRelevance.put(pageId, relativeRelevance);
        }
        mapPageIdRelRelevance.forEach((key, value) -> log.info("Страница № " + key + " - " + value + " относ релевантноть"));
        return mapPageIdRelRelevance;
    }
}
