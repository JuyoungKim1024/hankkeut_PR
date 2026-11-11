package back.backend.domain.jobposting.repository;

import back.backend.domain.jobposting.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    List<JobSkill> findAllByJobPostingIdOrderBySkillTypeAscSkillNameAsc(long jobPostingId);

    void deleteAllByJobPostingId(long jobPostingId);
}
