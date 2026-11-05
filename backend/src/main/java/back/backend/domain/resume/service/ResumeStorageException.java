package back.backend.domain.resume.service;

public class ResumeStorageException extends RuntimeException {

    public ResumeStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResumeStorageException(String message) {
        super(message);
    }
}
