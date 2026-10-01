package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.dto.ManualJobPostingImportRequest;
import back.backend.domain.jobposting.dto.ManualJobPostingImportResponse;
import back.backend.domain.jobposting.service.ManualJobPostingImportService;
import back.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs/imports")
public class ManualJobPostingImportController {

    private final ManualJobPostingImportService importService;

    public ManualJobPostingImportController(ManualJobPostingImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ManualJobPostingImportResponse>> importPosting(
            @Valid @RequestBody ManualJobPostingImportRequest request
    ) {
        ManualJobPostingImportResponse response = importService.importPosting(request);
        HttpStatus status = response.created() == 1 ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(response));
    }
}
