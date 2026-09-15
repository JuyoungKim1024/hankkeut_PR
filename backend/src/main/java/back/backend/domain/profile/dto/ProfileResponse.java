package back.backend.domain.profile.dto;

import back.backend.domain.profile.entity.CareerLevel;
import back.backend.domain.profile.entity.Profile;
import back.backend.domain.skill.entity.SkillLevel;
import back.backend.domain.skill.entity.UserSkill;

import java.util.List;

public record ProfileResponse(
        String desiredJob,
        CareerLevel careerLevel,
        String desiredLocation,
        List<SkillResponse> skills
) {
    public static ProfileResponse of(Profile profile, List<UserSkill> skills) {
        return new ProfileResponse(profile.getDesiredJob(), profile.getCareerLevel(), profile.getDesiredLocation(),
                skills.stream().map(skill -> new SkillResponse(skill.getSkillName(), skill.getSkillLevel())).toList());
    }

    public record SkillResponse(String name, SkillLevel level) {
    }
}
