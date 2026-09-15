package back.backend.domain.auth.oauth;

public record GoogleIdentity(String providerUserId, String email, String name, boolean emailVerified) {
}
