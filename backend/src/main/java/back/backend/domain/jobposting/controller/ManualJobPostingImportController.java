package back.backend.domain.jobposting.controller;

import back.backend.domain.jobposting.dto.ManualJobPostingImportRequest;
import back.backend.domain.jobposting.dto.ManualJobPostingImportResponse;
import back.backend.domain.jobposting.dto.UrlJobPostingImportRequest;
import back.backend.domain.jobposting.service.ManualJobPostingImportService;
import back.backend.domain.jobposting.service.UrlJobPostingImportService;
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
    private final UrlJobPostingImportService urlImportService;

    public ManualJobPostingImportController(ManualJobPostingImportService importService,
                                            UrlJobPostingImportService urlImportService) {
        this.importService = importService;
        this.urlImportService = urlImportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ManualJobPostingImportResponse>> importPosting(
            @Valid @RequestBody ManualJobPostingImportRequest request
    ) {
        ManualJobPostingImportResponse response = importService.importPosting(request);
        HttpStatus status = response.created() == 1 ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(response));
    }

    @PostMapping("/url")
    public ResponseEntity<ApiResponse<ManualJobPostingImportResponse>> importFromUrl(
            @Valid @RequestBody UrlJobPostingImportRequest request
    ) {
        ManualJobPostingImportResponse response = urlImportService.importFromUrl(request.originalUrl());
        HttpStatus status = response.created() == 1 ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(response));
    }
}
