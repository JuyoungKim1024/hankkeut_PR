package back.backend.domain.jobposting.source.saramin;

import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.domain.jobposting.source.JobPostingFetchResult;
import back.backend.domain.jobposting.source.JobPostingSource;
import back.backend.domain.jobposting.source.JobPostingSourceFailure;

import java.util.ArrayList;
import java.util.List;

public class SaraminJobPostingSource implements JobPostingSource {

    private static final int MAX_PAGE_SIZE = 110;

    private final SaraminApiClient apiClient;
    private final SaraminJobResponseParser responseParser;
    private final int pageSize;
    private final int maxPages;

    public SaraminJobPostingSource(SaraminApiClient apiClient,
                                   SaraminJobResponseParser responseParser,
                                   int pageSize,
                                   int maxPages) {
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize must be between 1 and 110");
        }
        if (maxPages < 1) {
            throw new IllegalArgumentException("maxPages must be positive");
        }
        this.apiClient = apiClient;
        this.responseParser = responseParser;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    @Override
    public JobSource source() {
        return JobSource.SARAMIN;
    }

    @Override
    public JobPostingFetchResult fetch() {
        List<CollectedJobPosting> postings = new ArrayList<>();
        List<JobPostingSourceFailure> failures = new ArrayList<>();
        int processed = 0;
        int total = 0;
        boolean complete = false;

        for (int page = 0; page < maxPages; page++) {
            SaraminJobPage result = responseParser.parse(apiClient.fetchPage(page, pageSize));
            postings.addAll(result.postings());
            failures.addAll(result.failures());
            processed += result.rawCount();
            total = result.total();

            if (processed >= total) {
                complete = true;
                break;
            }
            if (result.rawCount() == 0) {
                break;
            }
        }

        return new JobPostingFetchResult(postings, failures, complete);
    }
}
