package back.backend.domain.auth.dto;

public record AuthResponse(Long userId, String email, String name, String accessToken, long expiresInSeconds) {
}
