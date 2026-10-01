package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSource;

public record JobPostingCollectionResult(
        JobSource source,
        int fetched,
        int failed,
        int created,
        int updated,
        int unchanged,
        int closed
) {
}
