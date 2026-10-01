package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.source.JobPostingFetchResult;
import back.backend.domain.jobposting.source.JobPostingSource;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class JobPostingCollectionService {

    private final JobPostingIngestionService ingestionService;
    private final JobPostingLifecycleService lifecycleService;

    public JobPostingCollectionService(JobPostingIngestionService ingestionService,
                                       JobPostingLifecycleService lifecycleService) {
        this.ingestionService = ingestionService;
        this.lifecycleService = lifecycleService;
    }

    public JobPostingCollectionResult collect(JobPostingSource sourceAdapter) {
        JobSource source = sourceAdapter.source();
        JobPostingFetchResult fetchResult;
        try {
            fetchResult = sourceAdapter.fetch();
        } catch (RuntimeException exception) {
            throw new JobPostingCollectionException(source, exception);
        }

        JobPostingIngestionResult ingestion = ingestionService.ingest(source, fetchResult.postings());
        int closed = 0;
        if (fetchResult.completeSnapshot() && fetchResult.failures().isEmpty()) {
            closed = lifecycleService.closeMissing(
                    source,
                    fetchResult.postings().stream()
                            .map(posting -> posting.externalId())
                            .collect(Collectors.toUnmodifiableSet())
            );
        }
        return new JobPostingCollectionResult(
                source,
                fetchResult.postings().size(),
                fetchResult.failures().size(),
                ingestion.created(),
                ingestion.updated(),
                ingestion.unchanged(),
                closed
        );
    }
}
