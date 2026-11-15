package back.backend.domain.resume.repository;

import back.backend.domain.resume.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Long> {
    List<ResumeExperience> findAllByResumeIdOrderById(long resumeId);
    Optional<ResumeExperience> findByIdAndResumeId(long id, long resumeId);
}
