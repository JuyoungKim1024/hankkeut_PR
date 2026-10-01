package back.backend.domain.resume.service;

public class ResumeProcessingException extends RuntimeException {

    public ResumeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResumeProcessingException(String message) {
        super(message);
    }
}
