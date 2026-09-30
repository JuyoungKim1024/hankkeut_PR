package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class JobPostingLifecycleService {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingLifecycleService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional
    public int closeMissing(JobSource source, Set<String> seenExternalIds) {
        int closed = 0;
        for (JobPosting posting : jobPostingRepository.findAllBySourceAndStatus(source, JobPostingStatus.ACTIVE)) {
            if (!seenExternalIds.contains(posting.getExternalId())) {
                posting.close();
                closed++;
            }
        }
        return closed;
    }
}
