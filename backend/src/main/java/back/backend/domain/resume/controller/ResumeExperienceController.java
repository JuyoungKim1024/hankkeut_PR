package back.backend.domain.resume.controller;

import back.backend.domain.resume.dto.ExperienceInput;
import back.backend.domain.resume.service.ResumeExperienceService;
import back.backend.domain.resume.service.ResumeExperienceService.Experience;
import back.backend.domain.resume.service.ResumeExperienceService.Workspace;
import back.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes/{resumeId}/experiences")
public class ResumeExperienceController {
    private final ResumeExperienceService service;

    public ResumeExperienceController(ResumeExperienceService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<Workspace> get(@PathVariable long resumeId) {
        return ApiResponse.success(service.get(resumeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Experience> create(@PathVariable long resumeId, @Valid @RequestBody ExperienceInput input) {
        return ApiResponse.success(service.save(resumeId, input));
    }

    @PutMapping("/{experienceId}")
    public ApiResponse<Experience> update(@PathVariable long resumeId, @PathVariable long experienceId,
                                          @Valid @RequestBody ExperienceInput input) {
        return ApiResponse.success(service.update(resumeId, experienceId, input));
    }

    @DeleteMapping("/{experienceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long resumeId, @PathVariable long experienceId) {
        service.delete(resumeId, experienceId);
    }

}
