package back.backend.domain.application.controller;

import back.backend.domain.application.dto.ApplicationCreateRequest;
import back.backend.domain.application.dto.ApplicationResponse;
import back.backend.domain.application.dto.ApplicationUpdateRequest;
import back.backend.domain.application.entity.ApplicationStatus;
import back.backend.domain.application.service.ApplicationService;
import back.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(
            @Valid @RequestBody ApplicationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(applicationService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<ApplicationResponse>> findAll(
            @RequestParam(required = false) ApplicationStatus status
    ) {
        return ApiResponse.success(applicationService.findAll(status));
    }

    @PatchMapping("/{applicationId}")
    public ApiResponse<ApplicationResponse> update(
            @PathVariable long applicationId,
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        return ApiResponse.success(applicationService.update(applicationId, request));
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> delete(@PathVariable long applicationId) {
        applicationService.delete(applicationId);
        return ResponseEntity.noContent().build();
    }
}
