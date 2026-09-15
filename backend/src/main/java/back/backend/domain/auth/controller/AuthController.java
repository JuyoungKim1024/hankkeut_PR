package back.backend.domain.auth.controller;

import back.backend.domain.auth.config.AuthProperties;
import back.backend.domain.auth.dto.AuthResponse;
import back.backend.domain.auth.dto.LoginRequest;
import back.backend.domain.auth.dto.SignupRequest;
import back.backend.domain.auth.service.AuthService;
import back.backend.global.exception.ApiException;
import back.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String REFRESH_COOKIE = "refresh_token";

    private final AuthService authService;
    private final AuthProperties properties;

    public AuthController(AuthService authService, AuthProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request,
                                                             HttpServletResponse response) {
        AuthService.TokenResult result = authService.signup(request);
        addRefreshCookie(response, result.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result.response()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.TokenResult result = authService.login(request);
        addRefreshCookie(response, result.refreshToken());
        return ApiResponse.success(result.response());
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token이 유효하지 않습니다.");
        }
        AuthService.TokenResult result = authService.refresh(refreshToken);
        addRefreshCookie(response, result.refreshToken());
        return ApiResponse.success(result.response());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString());
        return ApiResponse.success(null);
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                refreshCookie(refreshToken, properties.refreshTokenTtl().toSeconds()).toString());
    }

    private ResponseCookie refreshCookie(String value, long maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}
