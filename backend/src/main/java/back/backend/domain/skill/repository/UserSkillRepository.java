package back.backend.domain.skill.repository;

import back.backend.domain.skill.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findAllByUserIdOrderByIdAsc(Long userId);
    void deleteAllByUserId(Long userId);
}
