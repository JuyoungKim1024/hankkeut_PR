package back.backend.domain.resume.service;

import back.backend.domain.resume.dto.ExperienceInput;
import back.backend.domain.resume.entity.Resume;
import back.backend.domain.resume.entity.ResumeExperience;
import back.backend.domain.resume.repository.ResumeExperienceRepository;
import back.backend.domain.resume.repository.ResumeRepository;
import back.backend.domain.user.service.LocalUserService;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResumeExperienceService {
    private final ResumeRepository resumes;
    private final ResumeExperienceRepository experiences;
    private final LocalUserService users;

    public ResumeExperienceService(ResumeRepository resumes, ResumeExperienceRepository experiences,
                                   LocalUserService users) {
        this.resumes = resumes;
        this.experiences = experiences;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Workspace get(long resumeId) {
        Resume resume = findResume(resumeId);
        return new Workspace(source(resume), experiences.findAllByResumeIdOrderById(resumeId)
                .stream().map(Experience::from).toList());
    }

    @Transactional
    public Experience save(long resumeId, ExperienceInput input) {
        Resume resume = findResume(resumeId);
        requireEvidence(resume, input.evidence());
        return Experience.from(experiences.save(new ResumeExperience(resume, input)));
    }

    @Transactional
    public Experience update(long resumeId, long experienceId, ExperienceInput input) {
        requireEvidence(findResume(resumeId), input.evidence());
        ResumeExperience experience = findExperience(resumeId, experienceId);
        experience.update(input);
        return Experience.from(experience);
    }

    @Transactional
    public void delete(long resumeId, long experienceId) {
        findResume(resumeId);
        experiences.delete(findExperience(resumeId, experienceId));
    }

    private Resume findResume(long id) {
        return users.find().flatMap(user -> resumes.findByIdAndUserId(id, user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "이력서를 찾을 수 없습니다."));
    }

    private ResumeExperience findExperience(long resumeId, long id) {
        return experiences.findByIdAndResumeId(id, resumeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EXPERIENCE_NOT_FOUND", "경험을 찾을 수 없습니다."));
    }

    private static String source(Resume resume) {
        return resume.getExtractedText() == null ? "" : resume.getExtractedText();
    }

    private static String requireEvidence(Resume resume, String excerpt) {
        String value = excerpt.strip();
        if (value.isEmpty() || !source(resume).contains(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EXPERIENCE_EVIDENCE_INVALID", "이력서 원문에서 근거 문장을 선택해 주세요.");
        }
        return value;
    }

    public record Workspace(String source, List<Experience> experiences) {}

    public record Experience(long id, ExperienceInput content) {
        static Experience from(ResumeExperience experience) {
            return new Experience(experience.id(), experience.content());
        }
    }
}
