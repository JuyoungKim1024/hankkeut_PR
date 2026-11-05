package back.backend.domain.resume.service;

public interface ResumeDocumentProcessor {

    ProcessedResumeDocument process(byte[] content, String originalFileName);
}
