package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobSkillType;

public record ExtractedJobSkill(String name, JobSkillType type) {
}
