package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.domain.jobposting.source.JobPostingFetchResult;
import back.backend.domain.jobposting.source.JobPostingSource;
import back.backend.domain.jobposting.source.JobPostingSourceFailure;
import back.backend.domain.jobposting.source.JobPostingSourceFailureCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class JobPostingCollectionServiceTest {

    @Autowired JobPostingCollectionService collectionService;
    @Autowired JobPostingRepository jobPostingRepository;

    @BeforeEach
    void setUp() {
        jobPostingRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 일부 항목이 실패해도 정상 공고를 저장하고 결과를 집계한다")
    void t1_partialFailureKeepsValidPostings() {
        JobPostingSource source = sourceReturning(new JobPostingFetchResult(
                List.of(posting()),
                List.of(new JobPostingSourceFailure("list-item-2", JobPostingSourceFailureCode.PARSE_ERROR)),
                false
        ));

        JobPostingCollectionResult result = collectionService.collect(source);

        assertThat(result).isEqualTo(new JobPostingCollectionResult(JobSource.MANUAL, 1, 1, 1, 0, 0, 0));
        assertThat(jobPostingRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("t2 소스 전체 호출이 실패하면 source가 포함된 안전한 예외를 반환한다")
    void t2_sourceFailureIsWrappedWithoutRawMessage() {
        JobPostingSource source = new JobPostingSource() {
            @Override
            public JobSource source() {
                return JobSource.MANUAL;
            }

            @Override
            public JobPostingFetchResult fetch() {
                throw new IllegalStateException("secret-response-body");
            }
        };

        assertThatThrownBy(() -> collectionService.collect(source))
                .isInstanceOf(JobPostingCollectionException.class)
                .hasMessage("MANUAL 공고 수집에 실패했습니다.")
                .message().doesNotContain("secret-response-body");
    }

    @Test
    @DisplayName("t3 불완전한 snapshot은 기존 활성 공고를 마감 처리하지 않는다")
    void t3_incompleteSnapshotDoesNotClosePosting() {
        collectionService.collect(sourceReturning(new JobPostingFetchResult(List.of(posting()), List.of(), true)));

        JobPostingCollectionResult result = collectionService.collect(
                sourceReturning(new JobPostingFetchResult(List.of(), List.of(), false)));

        assertThat(result.closed()).isZero();
        assertThat(jobPostingRepository.findAll().getFirst().getStatus()).isEqualTo(JobPostingStatus.ACTIVE);
    }

    @Test
    @DisplayName("t4 완전한 snapshot은 누락된 기존 활성 공고를 마감 처리한다")
    void t4_completeSnapshotClosesMissingPosting() {
        collectionService.collect(sourceReturning(new JobPostingFetchResult(List.of(posting()), List.of(), true)));

        JobPostingCollectionResult result = collectionService.collect(
                sourceReturning(new JobPostingFetchResult(List.of(), List.of(), true)));

        assertThat(result.closed()).isEqualTo(1);
        assertThat(jobPostingRepository.findAll().getFirst().getStatus()).isEqualTo(JobPostingStatus.CLOSED);
    }

    private static JobPostingSource sourceReturning(JobPostingFetchResult fetchResult) {
        return new JobPostingSource() {
            @Override
            public JobSource source() {
                return JobSource.MANUAL;
            }

            @Override
            public JobPostingFetchResult fetch() {
                return fetchResult;
            }
        };
    }

    private static CollectedJobPosting posting() {
        return new CollectedJobPosting(
                "external-1", "한끗 컴퍼니", "백엔드 개발자", "개발", "신입", "서울",
                "채용 설명", "Java", "Spring Boot", "https://example.com/jobs/1",
                LocalDateTime.of(2026, 9, 1, 0, 0),
                LocalDateTime.of(2026, 10, 1, 0, 0), JobPostingStatus.ACTIVE
        );
    }
}
