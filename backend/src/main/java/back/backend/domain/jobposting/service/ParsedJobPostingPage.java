package back.backend.domain.jobposting.service;

import java.time.LocalDateTime;

public record ParsedJobPostingPage(
        String companyName,
        String title,
        String career,
        String description,
        LocalDateTime expiredAt
) {

    public ParsedJobPostingPage(String title, String description) {
        this(null, title, null, description, null);
    }
}
