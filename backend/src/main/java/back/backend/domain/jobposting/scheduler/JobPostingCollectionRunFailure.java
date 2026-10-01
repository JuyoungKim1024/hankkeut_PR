package back.backend.domain.jobposting.scheduler;

import back.backend.domain.jobposting.entity.JobSource;

public record JobPostingCollectionRunFailure(
        JobSource source,
        String failureType
) {
}
