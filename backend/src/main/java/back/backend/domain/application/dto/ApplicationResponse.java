package back.backend.domain.application.dto;

import back.backend.domain.application.entity.Application;
import back.backend.domain.application.entity.ApplicationStatus;
import back.backend.domain.jobposting.entity.JobPosting;

import java.time.LocalDateTime;

public record ApplicationResponse(
        long id,
        long jobPostingId,
        String companyName,
        String jobTitle,
        String location,
        ApplicationStatus status,
        String memo,
        LocalDateTime nextActionAt,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt
) {

    public static ApplicationResponse from(Application application) {
        JobPosting jobPosting = application.getJobPosting();
        return new ApplicationResponse(
                application.getId(),
                jobPosting.getId(),
                jobPosting.getCompanyName(),
                jobPosting.getTitle(),
                jobPosting.getLocation(),
                application.getStatus(),
                application.getMemo(),
                application.getNextActionAt(),
                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }
}
