package back.backend.domain.resume.dto;

import back.backend.domain.resume.entity.Resume;
import back.backend.domain.resume.entity.ResumeFileType;

import java.time.LocalDateTime;

public record ResumeResponse(
        long id,
        String originalFileName,
        ResumeFileType fileType,
        int extractedTextLength,
        LocalDateTime createdAt
) {

    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getFileType(),
                resume.getExtractedText() == null ? 0 : resume.getExtractedText().length(),
                resume.getCreatedAt()
        );
    }
}
