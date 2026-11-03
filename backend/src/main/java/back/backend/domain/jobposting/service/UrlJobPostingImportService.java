package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.dto.ManualJobPostingImportResponse;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class UrlJobPostingImportService {

    private final JobPostingPageFetcher pageFetcher;
    private final JobPostingPageParser pageParser;
    private final JobPostingIngestionService ingestionService;
    private final JobPostingRepository jobPostingRepository;

    public UrlJobPostingImportService(JobPostingPageFetcher pageFetcher,
                                      JobPostingPageParser pageParser,
                                      JobPostingIngestionService ingestionService,
                                      JobPostingRepository jobPostingRepository) {
        this.pageFetcher = pageFetcher;
        this.pageParser = pageParser;
        this.ingestionService = ingestionService;
        this.jobPostingRepository = jobPostingRepository;
    }

    public ManualJobPostingImportResponse importFromUrl(String originalUrl) {
        FetchedJobPostingPage page = pageFetcher.fetch(originalUrl);
        ParsedJobPostingPage parsed = pageParser.parse(page);
        URI uri = page.uri();
        String normalizedUrl = uri.toString();
        String externalId = JobPostingUrlIdentity.hash(normalizedUrl);
        CollectedJobPosting posting = new CollectedJobPosting(
                externalId,
                companyName(uri),
                parsed.title(),
                "미분류",
                null,
                null,
                parsed.description(),
                null,
                null,
                normalizedUrl,
                null,
                null,
                JobPostingStatus.ACTIVE
        );

        JobPostingIngestionResult result = ingestionService.ingest(JobSource.WEB, List.of(posting));
        long jobPostingId = jobPostingRepository.findBySourceAndExternalId(JobSource.WEB, externalId)
                .orElseThrow(() -> new IllegalStateException("Imported job posting was not found"))
                .getId();
        return ManualJobPostingImportResponse.from(jobPostingId, result);
    }

    private String companyName(URI uri) {
        String host = uri.getHost();
        return host != null && host.startsWith("www.") ? host.substring(4) : host;
    }
}
