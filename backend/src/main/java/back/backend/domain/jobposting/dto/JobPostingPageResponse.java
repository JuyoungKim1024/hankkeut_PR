package back.backend.domain.jobposting.dto;

import back.backend.domain.jobposting.entity.JobPosting;
import org.springframework.data.domain.Page;

import java.util.List;

public record JobPostingPageResponse(
        List<JobPostingSummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static JobPostingPageResponse from(Page<JobPosting> result) {
        return new JobPostingPageResponse(
                result.getContent().stream().map(JobPostingSummaryResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
