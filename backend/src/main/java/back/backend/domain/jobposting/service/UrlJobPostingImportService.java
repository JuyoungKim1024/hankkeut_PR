package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.dto.ManualJobPostingImportResponse;
import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UrlJobPostingImportService {

    private final JobPostingPageFetcher pageFetcher;
    private final JobPostingPageParser pageParser;
    private final JobPostingIngestionService ingestionService;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingUrlPolicy urlPolicy;
    private final Duration refreshInterval;

    public UrlJobPostingImportService(JobPostingPageFetcher pageFetcher,
                                      JobPostingPageParser pageParser,
                                      JobPostingIngestionService ingestionService,
                                      JobPostingRepository jobPostingRepository,
                                      JobPostingUrlPolicy urlPolicy,
                                      @Value("${app.jobs.crawling.refresh-interval:PT6H}") Duration refreshInterval) {
        this.pageFetcher = pageFetcher;
        this.pageParser = pageParser;
        this.ingestionService = ingestionService;
        this.jobPostingRepository = jobPostingRepository;
        this.urlPolicy = urlPolicy;
        this.refreshInterval = refreshInterval;
    }

    public ManualJobPostingImportResponse importFromUrl(String originalUrl) {
        URI requestedUri = urlPolicy.validate(originalUrl);
        String requestedUrl = requestedUri.toString();
        String externalId = JobPostingUrlIdentity.hash(requestedUrl);
        var existing = jobPostingRepository.findBySourceAndExternalId(JobSource.WEB, externalId).orElse(null);
        if (isFresh(existing)) {
            return new ManualJobPostingImportResponse(existing.getId(), 0, 0, 1);
        }

        FetchedJobPostingPage page = pageFetcher.fetch(requestedUrl);
        ParsedJobPostingPage parsed = pageParser.parse(page);
        URI uri = page.uri();
        String normalizedUrl = uri.toString();
        externalId = JobPostingUrlIdentity.hash(normalizedUrl);
        CollectedJobPosting posting = new CollectedJobPosting(
                externalId,
                parsed.companyName() == null ? companyName(uri) : parsed.companyName(),
                parsed.title(),
                "미분류",
                parsed.career(),
                null,
                parsed.description(),
                null,
                null,
                normalizedUrl,
                null,
                parsed.expiredAt(),
                JobPostingStatus.ACTIVE
        );

        JobPostingIngestionResult result = ingestionService.ingest(JobSource.WEB, List.of(posting));
        long jobPostingId = jobPostingRepository.findBySourceAndExternalId(JobSource.WEB, externalId)
                .orElseThrow(() -> new IllegalStateException("Imported job posting was not found"))
                .getId();
        return ManualJobPostingImportResponse.from(jobPostingId, result);
    }

    private boolean isFresh(JobPosting posting) {
        return posting != null && posting.getLastFetchedAt() != null
                && posting.getLastFetchedAt().isAfter(LocalDateTime.now().minus(refreshInterval));
    }

    private String companyName(URI uri) {
        String host = uri.getHost();
        return host != null && host.startsWith("www.") ? host.substring(4) : host;
    }
}
