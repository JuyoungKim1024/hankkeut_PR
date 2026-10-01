package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JobPostingIngestionServiceTest {

    @Autowired JobPostingIngestionService ingestionService;
    @Autowired JobPostingRepository jobPostingRepository;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 동일 공고를 다시 수집하면 한 건을 유지하고 변경 없음으로 집계한다")
    void t1_samePostingIsIdempotent() {
        CollectedJobPosting posting = posting("백엔드 개발자");

        JobPostingIngestionResult first = ingestionService.ingest(JobSource.MANUAL, List.of(posting));
        JobPostingIngestionResult second = ingestionService.ingest(JobSource.MANUAL, List.of(posting));

        assertThat(first).isEqualTo(new JobPostingIngestionResult(1, 1, 0, 0));
        assertThat(second).isEqualTo(new JobPostingIngestionResult(1, 0, 0, 1));
        assertThat(jobPostingRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("t2 공고 내용이 변경되면 기존 행과 내용 hash를 갱신한다")
    void t2_changedPostingUpdatesExistingRow() {
        ingestionService.ingest(JobSource.MANUAL, List.of(posting("백엔드 개발자")));
        JobPosting before = jobPostingRepository.findAll().getFirst();
        String previousHash = before.getContentHash();

        JobPostingIngestionResult result = ingestionService.ingest(
                JobSource.MANUAL, List.of(posting("주니어 백엔드 개발자")));
        JobPosting updated = jobPostingRepository.findAll().getFirst();

        assertThat(result).isEqualTo(new JobPostingIngestionResult(1, 0, 1, 0));
        assertThat(jobPostingRepository.count()).isEqualTo(1);
        assertThat(updated.getTitle()).isEqualTo("주니어 백엔드 개발자");
        assertThat(updated.getContentHash()).isNotEqualTo(previousHash);
    }

    private static CollectedJobPosting posting(String title) {
        return new CollectedJobPosting(
                "external-1",
                "한끗 컴퍼니",
                title,
                "개발",
                "신입",
                "서울",
                "채용 설명",
                "Java",
                "Spring Boot",
                "https://example.com/jobs/1",
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 10, 1, 0, 0),
                JobPostingStatus.ACTIVE
        );
    }
}
