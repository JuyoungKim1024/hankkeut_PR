package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.dto.JobSkillResponse;
import back.backend.domain.jobposting.service.JobSkillAnalysisService;
import back.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/{jobId}/skills")
public class JobSkillController {

    private final JobSkillAnalysisService analysisService;

    public JobSkillController(JobSkillAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analysis")
    public ApiResponse<List<JobSkillResponse>> analyze(@PathVariable long jobId) {
        return ApiResponse.success(analysisService.analyze(jobId));
    }

    @GetMapping
    public ApiResponse<List<JobSkillResponse>> findAll(@PathVariable long jobId) {
        return ApiResponse.success(analysisService.findAll(jobId));
    }
}
