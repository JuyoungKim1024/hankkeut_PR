package back.backend.domain.jobposting.service;

public record JobPostingIngestionResult(
        int scanned,
        int created,
        int updated,
        int unchanged
) {
}
