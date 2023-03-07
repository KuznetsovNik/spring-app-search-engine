package searchengine.dto.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SiteDto {
    private String url;
    private String name;
    private Status status;
    private LocalDateTime statusTime;
    private String error;
    private int pages;
    private int lemmas;

    public static SiteDto getSiteDtoFromEntity(SiteEntity siteEntity){
        SiteDto siteDto = new SiteDto();
        siteDto.setUrl(siteEntity.getUrl());
        siteDto.setName(siteEntity.getName());
        siteDto.setStatus(siteEntity.getStatus());
        siteDto.setStatusTime(siteEntity.getStatusTime());
        siteDto.setError(siteEntity.getLastError());
        siteDto.setPages(siteEntity.getIndexPage().size());
        siteDto.setLemmas(siteEntity.getIndexLemma().size());

        return siteDto;
    }
}

