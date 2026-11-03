package back.backend.domain.application.repository;

import back.backend.domain.application.entity.Application;
import back.backend.domain.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    @EntityGraph(attributePaths = "jobPosting")
    List<Application> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "jobPosting")
    List<Application> findAllByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, ApplicationStatus status);

    @EntityGraph(attributePaths = "jobPosting")
    Optional<Application> findByIdAndUserId(Long id, Long userId);
}
