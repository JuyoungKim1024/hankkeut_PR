package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.dto.JobPostingDetailResponse;
import back.backend.domain.jobposting.dto.JobPostingPageResponse;
import back.backend.domain.jobposting.dto.JobPostingSort;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.service.JobPostingQueryService;
import back.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobPostingQueryController {

    private final JobPostingQueryService queryService;

    public JobPostingQueryController(JobPostingQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ApiResponse<JobPostingPageResponse> findAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "ACTIVE") JobPostingStatus status,
            @RequestParam(defaultValue = "LATEST") JobPostingSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(queryService.findAll(query, location, status, sort, page, size));
    }

    @GetMapping("/{jobId}")
    public ApiResponse<JobPostingDetailResponse> findById(@PathVariable long jobId) {
        return ApiResponse.success(queryService.findById(jobId));
    }
}
