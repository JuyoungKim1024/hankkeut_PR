package back.backend.domain.profile.controller;

import back.backend.domain.profile.dto.ProfileResponse;
import back.backend.domain.profile.dto.ProfileUpsertRequest;
import back.backend.domain.profile.service.ProfileService;
import back.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApiResponse<ProfileResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(profileService.get(Long.valueOf(jwt.getSubject())));
    }

    @PutMapping
    public ApiResponse<ProfileResponse> upsert(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ProfileUpsertRequest request) {
        return ApiResponse.success(profileService.upsert(Long.valueOf(jwt.getSubject()), request));
    }
}
