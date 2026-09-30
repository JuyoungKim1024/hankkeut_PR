package back.backend.domain.jobposting.service;

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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JobPostingLifecycleServiceTest {

    @Autowired JobPostingIngestionService ingestionService;
    @Autowired JobPostingLifecycleService lifecycleService;
    @Autowired JobPostingRepository jobPostingRepository;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
        ingestionService.ingest(JobSource.SARAMIN, List.of(posting()));
    }

    @Test
    @DisplayName("t1 완전한 snapshot에서 누락된 활성 공고를 마감 처리한다")
    void t1_completeSnapshotClosesMissingActivePosting() {
        int closed = lifecycleService.closeMissing(JobSource.SARAMIN, Set.of());

        assertThat(closed).isEqualTo(1);
        assertThat(jobPostingRepository.findAll().getFirst().getStatus()).isEqualTo(JobPostingStatus.CLOSED);
    }

    @Test
    @DisplayName("t2 확인된 공고는 완전한 snapshot에서도 활성 상태를 유지한다")
    void t2_seenPostingRemainsActive() {
        int closed = lifecycleService.closeMissing(JobSource.SARAMIN, Set.of("external-1"));

        assertThat(closed).isZero();
        assertThat(jobPostingRepository.findAll().getFirst().getStatus()).isEqualTo(JobPostingStatus.ACTIVE);
    }

    @Test
    @DisplayName("t3 마감 처리된 공고가 다시 수집되면 활성 상태로 복구한다")
    void t3_reappearedPostingBecomesActiveAgain() {
        lifecycleService.closeMissing(JobSource.SARAMIN, Set.of());

        ingestionService.ingest(JobSource.SARAMIN, List.of(posting()));

        assertThat(jobPostingRepository.findAll().getFirst().getStatus()).isEqualTo(JobPostingStatus.ACTIVE);
    }

    private static CollectedJobPosting posting() {
        return new CollectedJobPosting(
                "external-1", "한끗 컴퍼니", "백엔드 개발자", "개발", "신입", "서울",
                "채용 설명", "Java", null, "https://example.com/jobs/1",
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 10, 1, 0, 0), JobPostingStatus.ACTIVE
        );
    }
}
