package back.backend.domain.jobposting.scheduler;

import java.util.List;

public record JobPostingCollectionRunResult(
        int attempted,
        int succeeded,
        List<JobPostingCollectionRunFailure> failures,
        boolean skipped
) {

    public JobPostingCollectionRunResult {
        failures = List.copyOf(failures);
    }

    public static JobPostingCollectionRunResult skippedResult() {
        return new JobPostingCollectionRunResult(0, 0, List.of(), true);
    }
}
