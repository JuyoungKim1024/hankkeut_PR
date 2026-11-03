package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.dto.ManualJobPostingImportResponse;
import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlJobPostingImportServiceTest {

    @Mock JobPostingPageFetcher pageFetcher;
    @Mock JobPostingPageParser pageParser;
    @Mock JobPostingIngestionService ingestionService;
    @Mock JobPostingRepository jobPostingRepository;

    @Test
    @DisplayName("t1 URL에서 가져온 제목과 본문을 원문 링크와 함께 저장한다")
    void t1_fetchedPageIsStoredWithOriginalUrl() {
        URI uri = URI.create("https://jobs.example.com/jobs/123");
        FetchedJobPostingPage fetched = new FetchedJobPostingPage(
                uri, "text/html", "html".getBytes(StandardCharsets.UTF_8));
        when(pageFetcher.fetch(uri.toString())).thenReturn(fetched);
        when(pageParser.parse(fetched)).thenReturn(new ParsedJobPostingPage("백엔드 개발자", "채용 본문"));
        when(ingestionService.ingest(any(), any())).thenReturn(new JobPostingIngestionResult(1, 1, 0, 0));
        JobPosting saved = mock(JobPosting.class);
        when(saved.getId()).thenReturn(7L);
        when(jobPostingRepository.findBySourceAndExternalId(any(), any())).thenReturn(Optional.of(saved));
        UrlJobPostingImportService service = new UrlJobPostingImportService(
                pageFetcher, pageParser, ingestionService, jobPostingRepository);

        ManualJobPostingImportResponse response = service.importFromUrl(uri.toString());

        assertThat(response.jobPostingId()).isEqualTo(7L);
        assertThat(response.created()).isEqualTo(1);
        org.mockito.Mockito.verify(ingestionService).ingest(
                org.mockito.ArgumentMatchers.eq(JobSource.WEB),
                org.mockito.ArgumentMatchers.argThat(postings ->
                        postings.size() == 1
                                && postings.getFirst().originalUrl().equals(uri.toString())
                                && postings.getFirst().title().equals("백엔드 개발자")
                                && postings.getFirst().description().equals("채용 본문"))
        );
    }
}
