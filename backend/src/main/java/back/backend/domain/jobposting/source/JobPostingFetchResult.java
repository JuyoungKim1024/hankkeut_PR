package back.backend.domain.jobposting.source;

import java.util.List;

public record JobPostingFetchResult(
        List<CollectedJobPosting> postings,
        List<JobPostingSourceFailure> failures,
        boolean completeSnapshot
) {

    public JobPostingFetchResult {
        postings = List.copyOf(postings);
        failures = List.copyOf(failures);
    }
}
