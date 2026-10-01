package back.backend.domain.jobposting.source.saramin;

import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.domain.jobposting.source.JobPostingSourceFailure;

import java.util.List;

public record SaraminJobPage(
        int start,
        int total,
        int rawCount,
        List<CollectedJobPosting> postings,
        List<JobPostingSourceFailure> failures
) {

    public SaraminJobPage {
        postings = List.copyOf(postings);
        failures = List.copyOf(failures);
    }
}
