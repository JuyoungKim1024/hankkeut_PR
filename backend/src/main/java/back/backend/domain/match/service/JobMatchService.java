package back.backend.domain.match.service;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.jobposting.repository.JobSkillRepository;
import back.backend.domain.match.dto.JobMatchResponse;
import back.backend.domain.match.dto.JobMatchResponse.Status;
import back.backend.domain.match.entity.JobMatch;
import back.backend.domain.match.repository.JobMatchRepository;
import back.backend.domain.profile.repository.ProfileRepository;
import back.backend.domain.skill.repository.UserSkillRepository;
import back.backend.domain.user.entity.User;
import back.backend.domain.user.service.LocalUserService;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobMatchService {
    private static final String VERSION = "local-skill-match-v1";

    private final JobPostingRepository postings;
    private final JobSkillRepository jobSkills;
    private final JobMatchRepository matches;
    private final ProfileRepository profiles;
    private final UserSkillRepository userSkills;
    private final LocalUserService localUsers;

    public JobMatchService(JobPostingRepository postings, JobSkillRepository jobSkills, JobMatchRepository matches,
                           ProfileRepository profiles, UserSkillRepository userSkills, LocalUserService localUsers) {
        this.postings = postings;
        this.jobSkills = jobSkills;
        this.matches = matches;
        this.profiles = profiles;
        this.userSkills = userSkills;
        this.localUsers = localUsers;
    }

    @Transactional(readOnly = true)
    public JobMatchResponse get(long jobId) {
        if (!postings.existsById(jobId)) {
            throw jobNotFound();
        }
        return evaluate(jobId, null);
    }

    @Transactional
    public JobMatchResponse calculate(long jobId) {
        JobPosting posting = postings.findByIdForUpdate(jobId).orElseThrow(JobMatchService::jobNotFound);
        return evaluate(jobId, posting);
    }

    private JobMatchResponse evaluate(long jobId, JobPosting lockedPosting) {
        User user = localUsers.find().orElse(null);
        if (user == null || !profiles.existsByUserId(user.getId())) {
            return new JobMatchResponse(Status.PROFILE_REQUIRED, null);
        }
        var requirements = jobSkills.findAllByJobPostingIdOrderBySkillTypeAscSkillNameAsc(jobId);
        if (requirements.isEmpty()) {
            return new JobMatchResponse(Status.JOB_SKILLS_REQUIRED, null);
        }
        var comparison = JobMatchCalculator.compare(requirements, userSkills.findAllByUserIdOrderByIdAsc(user.getId()));
        String fingerprint = comparison.fingerprint();
        JobMatch saved = matches.findByUserIdAndJobPostingId(user.getId(), jobId).orElse(null);
        if (lockedPosting == null) {
            if (saved == null) {
                return new JobMatchResponse(Status.NOT_CALCULATED, null);
            }
            if (!saved.matchesInput(VERSION, fingerprint)) {
                return new JobMatchResponse(Status.STALE, null);
            }
            return ready(comparison, saved.getAnalyzedAt());
        }
        if (saved == null) {
            saved = JobMatch.create(user, lockedPosting);
        }
        LocalDateTime analyzedAt = LocalDateTime.now();
        saved.update(comparison.score(), comparison.names(true), comparison.names(false),
                comparison.explanation(), VERSION, fingerprint, analyzedAt);
        matches.save(saved);
        return ready(comparison, analyzedAt);
    }

    private static JobMatchResponse ready(JobMatchCalculator.Comparison comparison, LocalDateTime analyzedAt) {
        return new JobMatchResponse(Status.READY, new JobMatchResponse.Result(
                comparison.score(), comparison.matchedWeight(), comparison.totalWeight(), comparison.skills(),
                comparison.explanation(), VERSION, analyzedAt));
    }

    private static ApiException jobNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "채용공고를 찾을 수 없습니다.");
    }
}
