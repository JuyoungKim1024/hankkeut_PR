package back.backend.domain.match.controller;

import back.backend.domain.match.dto.JobMatchResponse;
import back.backend.domain.match.service.JobMatchService;
import back.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/{jobId}/match")
public class JobMatchController {
    private final JobMatchService service;

    public JobMatchController(JobMatchService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<JobMatchResponse> get(@PathVariable long jobId) {
        return ApiResponse.success(service.get(jobId));
    }

    @PostMapping
    public ApiResponse<JobMatchResponse> calculate(@PathVariable long jobId) {
        return ApiResponse.success(service.calculate(jobId));
    }
}
