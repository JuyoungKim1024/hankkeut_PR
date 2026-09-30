package back.backend.domain.jobposting.source;

import java.util.Objects;

public record JobPostingSourceFailure(
        String reference,
        JobPostingSourceFailureCode code
) {

    public JobPostingSourceFailure {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("reference must not be blank");
        }
        reference = reference.trim();
        Objects.requireNonNull(code, "code must not be null");
    }
}
