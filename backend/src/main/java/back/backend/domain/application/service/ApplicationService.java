package back.backend.domain.application.service;

import back.backend.domain.application.dto.ApplicationCreateRequest;
import back.backend.domain.application.dto.ApplicationResponse;
import back.backend.domain.application.dto.ApplicationUpdateRequest;
import back.backend.domain.application.entity.Application;
import back.backend.domain.application.entity.ApplicationStatus;
import back.backend.domain.application.repository.ApplicationRepository;
import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.domain.user.entity.User;
import back.backend.domain.user.service.LocalUserService;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final LocalUserService localUserService;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;

    public ApplicationService(LocalUserService localUserService,
                              JobPostingRepository jobPostingRepository,
                              ApplicationRepository applicationRepository) {
        this.localUserService = localUserService;
        this.jobPostingRepository = jobPostingRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public ApplicationResponse create(ApplicationCreateRequest request) {
        User user = localUserService.getOrCreate();
        JobPosting jobPosting = jobPostingRepository.findById(request.jobPostingId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "채용공고를 찾을 수 없습니다."));
        if (applicationRepository.existsByUserIdAndJobPostingId(user.getId(), jobPosting.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "APPLICATION_ALREADY_EXISTS",
                    "이미 지원 관리에 등록된 공고입니다.");
        }

        Application application = Application.create(
                user, jobPosting, request.status(), request.memo(), request.nextActionAt());
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    @Transactional
    public List<ApplicationResponse> findAll(ApplicationStatus status) {
        Long userId = localUserService.getOrCreate().getId();
        List<Application> applications = status == null
                ? applicationRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)
                : applicationRepository.findAllByUserIdAndStatusOrderByUpdatedAtDesc(userId, status);
        return applications.stream().map(ApplicationResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse update(long applicationId, ApplicationUpdateRequest request) {
        Long userId = localUserService.getOrCreate().getId();
        Application application = findOwned(applicationId, userId);
        ApplicationStatus nextStatus = request.status() == null ? application.getStatus() : request.status();
        if (!application.canTransitionTo(nextStatus)) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_APPLICATION_TRANSITION",
                    "현재 단계에서 선택한 지원 단계로 변경할 수 없습니다.");
        }
        application.update(nextStatus, request.memo(), request.nextActionAt());
        return ApplicationResponse.from(application);
    }

    @Transactional
    public void delete(long applicationId) {
        Long userId = localUserService.getOrCreate().getId();
        applicationRepository.delete(findOwned(applicationId, userId));
    }

    private Application findOwned(long applicationId, long userId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", "지원 기록을 찾을 수 없습니다."));
    }
}
