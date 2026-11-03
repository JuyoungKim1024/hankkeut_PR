package back.backend.domain.jobposting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UrlJobPostingImportRequest(
        @NotBlank
        @Size(max = 1000)
        @Pattern(regexp = "(?i)^https?://.+", message = "HTTP(S) URL이어야 합니다")
        String originalUrl
) {
}
