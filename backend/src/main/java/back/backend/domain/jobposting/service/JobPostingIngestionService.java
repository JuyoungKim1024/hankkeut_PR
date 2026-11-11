package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.repository.JobSkillRepository;
import back.backend.domain.jobposting.source.CollectedJobPosting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class JobPostingIngestionService {

    private final JobPostingRepository jobPostingRepository;
    private final JobSkillRepository jobSkillRepository;

    public JobPostingIngestionService(JobPostingRepository jobPostingRepository, JobSkillRepository jobSkillRepository) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobSkillRepository = jobSkillRepository;
    }

    @Transactional
    public JobPostingIngestionResult ingest(JobSource source, List<CollectedJobPosting> collectedPostings) {
        int created = 0;
        int updated = 0;
        int unchanged = 0;

        for (CollectedJobPosting collected : collectedPostings) {
            String contentHash = JobPostingContentHasher.hash(collected);
            JobPosting existing = jobPostingRepository
                    .findIdBySourceAndExternalId(source, collected.externalId())
                    .flatMap(jobPostingRepository::findByIdForUpdate)
                    .orElse(null);

            if (existing == null) {
                jobPostingRepository.save(JobPosting.create(source, collected, contentHash));
                created++;
            } else if (existing.hasContentHash(contentHash)) {
                unchanged++;
            } else {
                if (skillSourceChanged(existing, collected)) {
                    jobSkillRepository.deleteAllByJobPostingId(existing.getId());
                }
                existing.update(collected, contentHash);
                updated++;
            }
        }

        return new JobPostingIngestionResult(collectedPostings.size(), created, updated, unchanged);
    }

    private boolean skillSourceChanged(JobPosting existing, CollectedJobPosting collected) {
        return !Objects.equals(existing.getDescription(), collected.description())
                || !Objects.equals(existing.getQualification(), collected.qualification())
                || !Objects.equals(existing.getPreference(), collected.preference());
    }
}
