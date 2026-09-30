package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.source.JobPostingFetchResult;
import back.backend.domain.jobposting.source.JobPostingSource;
import org.springframework.stereotype.Service;

@Service
public class JobPostingCollectionService {

    private final JobPostingIngestionService ingestionService;

    public JobPostingCollectionService(JobPostingIngestionService ingestionService) {
        this.ingestionService = ingestionService;
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
        return new JobPostingCollectionResult(
                source,
                fetchResult.postings().size(),
                fetchResult.failures().size(),
                ingestion.created(),
                ingestion.updated(),
                ingestion.unchanged()
        );
    }
}
