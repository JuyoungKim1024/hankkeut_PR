package back.backend.domain.jobposting.dto;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;

import java.time.LocalDateTime;

public record JobPostingSummaryResponse(
        long id,
        JobSource source,
        String companyName,
        String title,
        String jobCategory,
        String career,
        String location,
        String originalUrl,
        LocalDateTime postedAt,
        LocalDateTime expiredAt,
        JobPostingStatus status
) {

    public static JobPostingSummaryResponse from(JobPosting posting) {
        return new JobPostingSummaryResponse(
                posting.getId(),
                posting.getSource(),
                posting.getCompanyName(),
                posting.getTitle(),
                posting.getJobCategory(),
                posting.getCareer(),
                posting.getLocation(),
                posting.getOriginalUrl(),
                posting.getPostedAt(),
                posting.getExpiredAt(),
                posting.getStatus()
        );
    }
}
