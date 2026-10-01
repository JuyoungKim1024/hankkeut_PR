package back.backend.domain.jobposting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ManualJobPostingImportRequest(
        @NotBlank
        @Size(max = 1000)
        @Pattern(regexp = "(?i)^https?://.+", message = "HTTP(S) URL이어야 합니다")
        String originalUrl,
        @NotBlank @Size(max = 255) String companyName,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100) String jobCategory,
        @Size(max = 100) String career,
        @Size(max = 255) String location,
        @NotBlank @Size(max = 1_000_000) String description,
        @Size(max = 1_000_000) String qualification,
        @Size(max = 1_000_000) String preference,
        LocalDateTime postedAt,
        LocalDateTime expiredAt
) {
}
