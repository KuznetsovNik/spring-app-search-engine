package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.SiteDto;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        int countsSite = (int) siteRepository.count();
        total.setSites(countsSite);
        total.setIndexing(false);
        Optional<SiteEntity> optionalSite = siteRepository.findById(countsSite);
        if (optionalSite.isPresent()) {
            if (optionalSite.get().getStatus() == Status.INDEXING) {
                total.setIndexing(true);
            }
        }

        List<SiteDto> detailed = new ArrayList<>();
        int totalPages = 0;
        int totalLemmas = 0;
        for(int i = 1; i <= siteRepository.count(); i++) {
            Optional<SiteEntity> optionalSiteEntity = siteRepository.findById(i);
            if (optionalSiteEntity.isPresent()) {
                SiteEntity siteEntity = optionalSiteEntity.get();
                SiteDto siteDto = SiteDto.getSiteDtoFromEntity(siteEntity);
                detailed.add(siteDto);
                totalPages += siteDto.getPages();
                totalLemmas += siteDto.getLemmas();
            }
        }
        total.setPages(totalPages);
        total.setLemmas(totalLemmas);

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
