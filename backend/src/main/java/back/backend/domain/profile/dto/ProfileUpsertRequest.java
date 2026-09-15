package back.backend.domain.profile.dto;

import back.backend.domain.profile.entity.CareerLevel;
import back.backend.domain.skill.entity.SkillLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProfileUpsertRequest(
        @NotBlank @Size(max = 100) String desiredJob,
        @NotNull CareerLevel careerLevel,
        @NotBlank @Size(max = 100) String desiredLocation,
        @NotNull @Size(max = 50) List<@Valid SkillRequest> skills
) {
    public record SkillRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull SkillLevel level
    ) {
    }
}
