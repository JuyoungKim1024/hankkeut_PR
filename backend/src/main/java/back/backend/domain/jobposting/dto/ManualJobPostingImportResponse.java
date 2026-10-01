package back.backend.domain.jobposting.dto;

import back.backend.domain.jobposting.service.JobPostingIngestionResult;

public record ManualJobPostingImportResponse(
        long jobPostingId,
        int created,
        int updated,
        int unchanged
) {

    public static ManualJobPostingImportResponse from(long jobPostingId, JobPostingIngestionResult result) {
        return new ManualJobPostingImportResponse(
                jobPostingId,
                result.created(),
                result.updated(),
                result.unchanged()
        );
    }
}
