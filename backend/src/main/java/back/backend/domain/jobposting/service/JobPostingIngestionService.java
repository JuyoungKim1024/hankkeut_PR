package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobPostingIngestionService {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingIngestionService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional
    public JobPostingIngestionResult ingest(JobSource source, List<CollectedJobPosting> collectedPostings) {
        int created = 0;
        int updated = 0;
        int unchanged = 0;

        for (CollectedJobPosting collected : collectedPostings) {
            String contentHash = JobPostingContentHasher.hash(collected);
            JobPosting existing = jobPostingRepository
                    .findBySourceAndExternalId(source, collected.externalId())
                    .orElse(null);

            if (existing == null) {
                jobPostingRepository.save(JobPosting.create(source, collected, contentHash));
                created++;
            } else if (existing.hasContentHash(contentHash)) {
                unchanged++;
            } else {
                existing.update(collected, contentHash);
                updated++;
            }
        }

        return new JobPostingIngestionResult(collectedPostings.size(), created, updated, unchanged);
    }
}
