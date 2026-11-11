package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.dto.JobSkillResponse;
import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobSkill;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.repository.JobSkillRepository;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobSkillAnalysisService {

    private final JobPostingRepository jobPostingRepository;
    private final JobSkillRepository jobSkillRepository;
    private final RuleBasedSkillExtractor skillExtractor;

    public JobSkillAnalysisService(JobPostingRepository jobPostingRepository,
                                   JobSkillRepository jobSkillRepository,
                                   RuleBasedSkillExtractor skillExtractor) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.skillExtractor = skillExtractor;
    }

    @Transactional
    public List<JobSkillResponse> analyze(long jobPostingId) {
        JobPosting posting = jobPostingRepository.findByIdForUpdate(jobPostingId)
                .orElseThrow(JobSkillAnalysisService::postingNotFound);
        List<ExtractedJobSkill> extracted = skillExtractor.extract(
                posting.getDescription(), posting.getQualification(), posting.getPreference());

        jobSkillRepository.deleteAllByJobPostingId(jobPostingId);
        jobSkillRepository.flush();
        List<JobSkill> saved = jobSkillRepository.saveAll(extracted.stream()
                .map(skill -> JobSkill.create(posting, skill.name(), skill.type()))
                .toList());
        return saved.stream().map(JobSkillResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<JobSkillResponse> findAll(long jobPostingId) {
        if (!jobPostingRepository.existsById(jobPostingId)) {
            throw postingNotFound();
        }
        return jobSkillRepository.findAllByJobPostingIdOrderBySkillTypeAscSkillNameAsc(jobPostingId).stream()
                .map(JobSkillResponse::from)
                .toList();
    }

    private static ApiException postingNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "채용공고를 찾을 수 없습니다.");
    }
}
