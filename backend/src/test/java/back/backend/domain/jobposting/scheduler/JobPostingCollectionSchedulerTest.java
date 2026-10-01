package back.backend.domain.jobposting.scheduler;

import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.service.JobPostingCollectionException;
import back.backend.domain.jobposting.service.JobPostingCollectionResult;
import back.backend.domain.jobposting.service.JobPostingCollectionService;
import back.backend.domain.jobposting.source.JobPostingSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobPostingCollectionSchedulerTest {

    @Test
    @DisplayName("t1 수집 실행 중 다시 호출하면 중복 실행을 건너뛴다")
    void t1_concurrentRunIsSkipped() throws Exception {
        JobPostingCollectionService collectionService = mock(JobPostingCollectionService.class);
        JobPostingSource source = source();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(collectionService.collect(source)).thenAnswer(invocation -> {
            entered.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test timeout");
            }
            return successResult();
        });
        JobPostingCollectionScheduler scheduler = new JobPostingCollectionScheduler(List.of(source), collectionService);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<JobPostingCollectionRunResult> firstRun = executor.submit(scheduler::runOnce);
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();

            JobPostingCollectionRunResult duplicate = scheduler.runOnce();
            release.countDown();

            assertThat(duplicate.skipped()).isTrue();
            assertThat(firstRun.get(5, TimeUnit.SECONDS).succeeded()).isEqualTo(1);
            verify(collectionService).collect(source);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("t2 한 source가 실패해도 다음 source를 계속 실행한다")
    void t2_sourceFailureDoesNotStopRemainingSources() {
        JobPostingCollectionService collectionService = mock(JobPostingCollectionService.class);
        JobPostingSource failedSource = source();
        JobPostingSource successfulSource = source();
        when(collectionService.collect(failedSource))
                .thenThrow(new JobPostingCollectionException(JobSource.SARAMIN,
                        new IllegalStateException("secret-response-body")));
        when(collectionService.collect(successfulSource)).thenReturn(successResult());
        JobPostingCollectionScheduler scheduler = new JobPostingCollectionScheduler(
                List.of(failedSource, successfulSource), collectionService);

        JobPostingCollectionRunResult result = scheduler.runOnce();

        assertThat(result.attempted()).isEqualTo(2);
        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.failures()).containsExactly(
                new JobPostingCollectionRunFailure(JobSource.SARAMIN, "IllegalStateException"));
        assertThat(result.toString()).doesNotContain("secret-response-body");
        verify(collectionService).collect(successfulSource);
    }

    private static JobPostingSource source() {
        JobPostingSource source = mock(JobPostingSource.class);
        when(source.source()).thenReturn(JobSource.SARAMIN);
        return source;
    }

    private static JobPostingCollectionResult successResult() {
        return new JobPostingCollectionResult(JobSource.SARAMIN, 1, 0, 1, 0, 0, 0);
    }
}
