package back.backend.domain.auth.oauth;

import back.backend.domain.auth.service.AuthService;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(GoogleIdentityClient.class)
public class GoogleOAuthService {

    private final GoogleIdentityClient googleIdentityClient;
    private final AuthService authService;

    public GoogleOAuthService(GoogleIdentityClient googleIdentityClient, AuthService authService) {
        this.googleIdentityClient = googleIdentityClient;
        this.authService = authService;
    }

    public AuthService.TokenResult login(String authorizationCode, String redirectUri) {
        GoogleIdentity identity = googleIdentityClient.exchange(authorizationCode, redirectUri);
        if (!identity.emailVerified()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "GOOGLE_EMAIL_NOT_VERIFIED",
                    "Google에서 확인된 이메일이 필요합니다.");
        }
        return authService.loginGoogle(identity.providerUserId(), identity.email(), identity.name());
    }
}
