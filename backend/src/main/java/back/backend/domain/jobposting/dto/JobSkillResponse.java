package back.backend.domain.jobposting.dto;

import back.backend.domain.jobposting.entity.JobSkill;
import back.backend.domain.jobposting.entity.JobSkillType;

public record JobSkillResponse(String name, JobSkillType type) {

    public static JobSkillResponse from(JobSkill skill) {
        return new JobSkillResponse(skill.getSkillName(), skill.getSkillType());
    }
}
