package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    SearchResponse searchResponse = new SearchResponse();
    static LuceneMorphology luceneMorph;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Override
    public SearchResponse search(String searchQuery, String searchSite, int searchOffset, int searchLimit){
        try {
            luceneMorph = new RussianLuceneMorphology();
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                searchResponse.setResult(false);
                searchResponse.setError("Задан пустой поисковый запрос");
                return searchResponse;
            }

            if (searchSite != null) {
                searchSite += "/";
                System.out.println("Ведем поиск по сайту: " + searchSite);
                SiteEntity siteEntity = siteRepository.findByUrl(searchSite);
                if (siteEntity != null) {
                    int siteId = siteEntity.getSiteId();
                    List<Map.Entry<String, Integer>> list = getListUniqueLemmas(searchQuery, siteId);
                }else {
                    searchResponse.setResult(false);
                    searchResponse.setError("Указанная страница не найдена");
                    return searchResponse;
                }
            }else{
                int siteId = 0;
                List<Map.Entry<String, Integer>> list = getListUniqueLemmas(searchQuery, siteId);
            }

            // TODO ЕСТЬ ОШИБКА В ЗАПИСИ САЙТА В БАЗУ ДАННЫХ, там разная валидация строки с www и без
            //  из за это много лем не попадает и не проходит проверка страницу на пренадлежность к сайту

            // TODO: TASK По первой, самой редкой лемме из списка, находить все страницы,
            //  на которых она встречается. Далее искать соответствия следующей леммы из этого списка страниц,
            //  а затем повторять операцию по каждой следующей лемме. Список страниц при этом на каждой итерации
            //  должен уменьшаться.







            searchResponse.setResult(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchResponse;
    }

    public List<Map.Entry<String, Integer>> getListUniqueLemmas(String searchQuery, int siteId){
        String [] queryArray = searchQuery.toLowerCase().split("([^а-яa-z0-9ё]+)");
        Map<String,Integer> wordsMap = new HashMap<>();
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
                            wordsMap.put(normalWord, lemma.getFrequency());
                        }
                    }
                }else{
                    lemma = lemmaRepository.findByLemma(normalWord);
                    if (lemma != null) {
                        wordsMap.put(normalWord, lemma.getFrequency());
                    }
                }
            }
        }

        List<Map.Entry<String, Integer>> list = wordsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        list.removeIf(el-> el.getValue() > 5);
        System.out.println(list);
        return list;
    }
}
/**
 Параметры:
 * query — поисковый запрос;
 * site — сайт, по которому осуществлять поиск
 (если не задан, поиск должен происходить по всем проиндексированным сайтам);
 задаётся в формате адреса, например: http://www.site.com (без слэша в конце);
 * offset — сдвиг от 0 для постраничного вывода (параметр необязательный;
 если не установлен, то значение по умолчанию равно нулю);
 * limit — количество результатов, которое необходимо вывести
 (параметр необязательный; если не установлен, то значение по умолчанию равно 20).
 * {
 * 	'result': false,
 * 	'error': "Задан пустой поисковый запрос"
 * }
 * {
 * 	'result': false,
 * 	'error': "Указанная страница не найдена"
 * }
 */