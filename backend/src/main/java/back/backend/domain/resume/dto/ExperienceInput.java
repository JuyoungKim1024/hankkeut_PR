package back.backend.domain.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExperienceInput(
        @NotBlank @Size(max = 150) String project,
        @NotBlank @Size(max = 150) String role,
        @Size(max = 300) String skills,
        @Size(max = 500) String outcome,
        @NotBlank @Size(max = 1000) String evidence) {}
