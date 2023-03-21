package searchengine.dto.search;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.services.indexing.LemmaFinderServiceIml;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Log4j2
@Data
public class PageDto implements Comparable<PageDto>{
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
    private static LuceneMorphology luceneMorph;

    public static PageDto getPageDtoFromEntity(PageEntity pageEntity, String lemma, float relevance){
        PageDto pageDto = new PageDto();
        pageDto.setSite(pageEntity.getSite().getUrl().replaceAll("\\/{1}",""));
        pageDto.setSiteName(pageEntity.getSite().getName());
        pageDto.setUri(pageEntity.getPath());
        String htmlContent = pageEntity.getContent();
        Document doc = Jsoup.parse(htmlContent);
        String title = doc.title();
        String snippet = getSnippet(pageEntity, lemma);
        pageDto.setTitle(title);
        pageDto.setSnippet(snippet);
        pageDto.setRelevance(relevance);
        return pageDto;
    }

    private static String getSnippet(PageEntity page,String lemma){
        String snippet = LemmaFinderServiceIml.cleaningHTMLContent(page);
        int indexLemma = snippet.indexOf(findLemmaInContext(page,lemma));
        int size = snippet.length();
        if (size < indexLemma + 200){
            size = size - indexLemma;
        }else{
            size = 200;
        }
        if (indexLemma == -1){
            indexLemma = size / 2;
        }
        StringBuilder outputBuilder = new StringBuilder();
        String snippetFirstPart = snippet.substring(indexLemma - size/2 , indexLemma);
        String word = snippet.substring(indexLemma,indexLemma + lemma.length());
        String snippetEndPart = snippet.substring(indexLemma + lemma.length(), indexLemma + size/2);
        outputBuilder.append(snippetFirstPart).append("<b>").append(word).append("</b>").append(snippetEndPart);
        return outputBuilder.toString();
    }

    @Override
    public int compareTo(PageDto anotherPageDto) {
        return Float.compare(anotherPageDto.relevance, this.relevance);
    }

    public void calculateRelToAbsRelevance(float biggerRelevance){
        this.setRelevance(this.getRelevance()/biggerRelevance);
    }

    public static String findLemmaInContext(PageEntity page, String lemma){
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        String content = LemmaFinderServiceIml.cleaningHTMLContent(page);
        String[] words = content.toLowerCase(Locale.ROOT).split("[^а-я]");
        for (String word : words) {
            if (!word.isEmpty()) {
                List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
                if (!wordBaseForms.stream().allMatch(e -> e.toUpperCase().contains("СОЮЗ") || e.toUpperCase().contains("МЕЖД") || e.toUpperCase().contains("ПРЕДЛ"))) {
                    List<String> normalForms = luceneMorph.getNormalForms(word);
                    if (normalForms.isEmpty()) {
                        continue;
                    }
                    String normalWord = normalForms.get(0);
                    if (normalWord.equals(lemma.toLowerCase())) {
                        return word;
                    }
                }
            }
        }
        return lemma;
    }
}


