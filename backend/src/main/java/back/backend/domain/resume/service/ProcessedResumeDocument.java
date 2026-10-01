package back.backend.domain.resume.service;

import back.backend.domain.resume.entity.ResumeFileType;

public record ProcessedResumeDocument(
        ResumeFileType fileType,
        String extractedText
) {
}
