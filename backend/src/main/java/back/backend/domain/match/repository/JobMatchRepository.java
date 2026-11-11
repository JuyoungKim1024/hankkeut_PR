package back.backend.domain.match.repository;

import back.backend.domain.match.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {
    Optional<JobMatch> findByUserIdAndJobPostingId(long userId, long jobPostingId);
}
