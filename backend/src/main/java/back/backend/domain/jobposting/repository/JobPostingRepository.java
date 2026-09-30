package back.backend.domain.jobposting.repository;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    Optional<JobPosting> findBySourceAndExternalId(JobSource source, String externalId);
}
