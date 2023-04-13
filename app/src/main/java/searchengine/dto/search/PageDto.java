package searchengine.dto.search;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.PageEntity;

import java.io.IOException;
import java.util.*;

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
        Document document = Jsoup.parse(page.getContent());
        String content = Jsoup.parse(document.html()).text().toLowerCase(Locale.ROOT);
        String rareWord = getIndexLemma(content,lemma);
        int indexLemma = content.indexOf(rareWord);
        int lemmaLength = rareWord.length();
        int size = content.length();
        if (size <= indexLemma + 200) {
            size = size - indexLemma;
        } else {
            size = 200;
        }
        int startSnippet = indexLemma - size/2;
        if (startSnippet < 0) {
            startSnippet = 0;
        }
        int endSnippet = indexLemma + size/2;
        if (endSnippet < indexLemma + lemmaLength) {
            endSnippet = content.length();
        }
        StringBuilder outputBuilder = new StringBuilder();
        String snippetFirstPart = content.substring(startSnippet , indexLemma);
        String word = content.substring(indexLemma,indexLemma + lemmaLength);
        String snippetEndPart = content.substring(indexLemma + lemmaLength, endSnippet);
        outputBuilder.append(snippetFirstPart).append("<b>").append(word).append("</b>").append(snippetEndPart);
        String snippet = outputBuilder.substring(0,outputBuilder.toString().lastIndexOf(" "));
        return snippet.replaceFirst("([а-яa-z\\,\\:\\!\\.]+)\\s{1}","");
    }

    @Override
    public int compareTo(PageDto anotherPageDto) {
        return Float.compare(anotherPageDto.relevance, this.relevance);
    }

    public void calculateRelToAbsRelevance(float biggerRelevance){
        this.setRelevance(this.getRelevance()/biggerRelevance);
    }

    public static String getIndexLemma(String content, String lemma){
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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


