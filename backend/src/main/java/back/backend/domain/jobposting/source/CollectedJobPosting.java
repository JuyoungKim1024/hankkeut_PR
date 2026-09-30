package back.backend.domain.jobposting.source;

import back.backend.domain.jobposting.entity.JobPostingStatus;

import java.net.URI;
import java.time.LocalDateTime;

public record CollectedJobPosting(
        String externalId,
        String companyName,
        String title,
        String jobCategory,
        String career,
        String location,
        String description,
        String qualification,
        String preference,
        String originalUrl,
        LocalDateTime postedAt,
        LocalDateTime expiredAt,
        JobPostingStatus status
) {

    public CollectedJobPosting {
        externalId = requireText(externalId, "externalId");
        companyName = requireText(companyName, "companyName");
        title = requireText(title, "title");
        jobCategory = requireText(jobCategory, "jobCategory");
        career = normalizeOptional(career);
        location = normalizeOptional(location);
        description = requireText(description, "description");
        qualification = normalizeOptional(qualification);
        preference = normalizeOptional(preference);
        originalUrl = normalizeUrl(originalUrl);
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }

    private static String requireText(String value, String field) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeUrl(String value) {
        String normalized = requireText(value, "originalUrl");
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("originalUrl must be a valid HTTP URL", exception);
        }
        if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("originalUrl must be a valid HTTP URL");
        }
        return normalized;
    }
}
