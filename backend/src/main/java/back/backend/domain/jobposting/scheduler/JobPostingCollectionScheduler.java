package back.backend.domain.jobposting.scheduler;

import back.backend.domain.jobposting.service.JobPostingCollectionException;
import back.backend.domain.jobposting.service.JobPostingCollectionService;
import back.backend.domain.jobposting.source.JobPostingSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class JobPostingCollectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobPostingCollectionScheduler.class);

    private final List<JobPostingSource> sources;
    private final JobPostingCollectionService collectionService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public JobPostingCollectionScheduler(List<JobPostingSource> sources,
                                         JobPostingCollectionService collectionService) {
        this.sources = List.copyOf(sources);
        this.collectionService = collectionService;
    }

    @Scheduled(
            fixedDelayString = "${app.jobs.collection.fixed-delay:PT6H}",
            initialDelayString = "${app.jobs.collection.initial-delay:PT30S}"
    )
    public JobPostingCollectionRunResult runOnce() {
        if (!running.compareAndSet(false, true)) {
            log.info("공고 수집을 건너뜁니다: 이전 실행이 진행 중입니다.");
            return JobPostingCollectionRunResult.skippedResult();
        }

        int succeeded = 0;
        List<JobPostingCollectionRunFailure> failures = new ArrayList<>();
        try {
            for (JobPostingSource source : sources) {
                try {
                    collectionService.collect(source);
                    succeeded++;
                } catch (JobPostingCollectionException exception) {
                    failures.add(new JobPostingCollectionRunFailure(
                            exception.getSource(), exception.getFailureType()));
                    log.warn("공고 수집 실패: source={}, type={}",
                            exception.getSource(), exception.getFailureType());
                } catch (RuntimeException exception) {
                    failures.add(new JobPostingCollectionRunFailure(
                            source.source(), exception.getClass().getSimpleName()));
                    log.warn("공고 수집 실패: source={}, type={}",
                            source.source(), exception.getClass().getSimpleName());
                }
            }
            log.info("공고 수집 완료: attempted={}, succeeded={}, failed={}",
                    sources.size(), succeeded, failures.size());
            return new JobPostingCollectionRunResult(sources.size(), succeeded, failures, false);
        } finally {
            running.set(false);
        }
    }
}
