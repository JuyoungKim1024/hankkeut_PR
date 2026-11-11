package back.backend.domain.match.dto;

import back.backend.domain.jobposting.entity.JobSkillType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record JobMatchResponse(Status status, Result result) {
    public enum Status {
        PROFILE_REQUIRED, JOB_SKILLS_REQUIRED, NOT_CALCULATED, STALE, READY
    }

    public record Result(BigDecimal score, int matchedWeight, int totalWeight, List<Skill> skills,
                         String explanation, String version, LocalDateTime analyzedAt) {
    }

    public record Skill(String name, JobSkillType type, boolean matched) {
    }
}
