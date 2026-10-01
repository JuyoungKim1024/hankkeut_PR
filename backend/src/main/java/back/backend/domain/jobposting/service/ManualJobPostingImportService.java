package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.dto.ManualJobPostingImportRequest;
import back.backend.domain.jobposting.dto.ManualJobPostingImportResponse;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class ManualJobPostingImportService {

    private final JobPostingIngestionService ingestionService;
    private final JobPostingRepository jobPostingRepository;

    public ManualJobPostingImportService(JobPostingIngestionService ingestionService,
                                         JobPostingRepository jobPostingRepository) {
        this.ingestionService = ingestionService;
        this.jobPostingRepository = jobPostingRepository;
    }

    public ManualJobPostingImportResponse importPosting(ManualJobPostingImportRequest request) {
        String originalUrl = normalizeUrl(request.originalUrl());
        CollectedJobPosting posting = new CollectedJobPosting(
                hashUrl(originalUrl),
                request.companyName(),
                request.title(),
                request.jobCategory(),
                request.career(),
                request.location(),
                request.description(),
                request.qualification(),
                request.preference(),
                originalUrl,
                request.postedAt(),
                request.expiredAt(),
                JobPostingStatus.ACTIVE
        );

        JobPostingIngestionResult result = ingestionService.ingest(JobSource.MANUAL, List.of(posting));
        long jobPostingId = jobPostingRepository
                .findBySourceAndExternalId(JobSource.MANUAL, posting.externalId())
                .orElseThrow(() -> new IllegalStateException("Imported job posting was not found"))
                .getId();

        return ManualJobPostingImportResponse.from(jobPostingId, result);
    }

    private String normalizeUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw invalidUrl();
            }
            String normalized = uri.normalize().toString();
            int fragmentStart = normalized.indexOf('#');
            return fragmentStart < 0 ? normalized : normalized.substring(0, fragmentStart);
        } catch (IllegalArgumentException exception) {
            throw invalidUrl();
        }
    }

    private String hashUrl(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private ApiException invalidUrl() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "유효한 HTTP(S) URL을 입력해 주세요.");
    }
}
