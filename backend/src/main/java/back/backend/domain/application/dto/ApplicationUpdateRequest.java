package back.backend.domain.application.dto;

import back.backend.domain.application.entity.ApplicationStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ApplicationUpdateRequest(
        ApplicationStatus status,
        @Size(max = 5000) String memo,
        LocalDateTime nextActionAt
) {
}
