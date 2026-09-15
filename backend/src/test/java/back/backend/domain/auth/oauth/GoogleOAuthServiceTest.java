package back.backend.domain.auth.oauth;

import back.backend.domain.auth.service.AuthService;
import back.backend.global.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock GoogleIdentityClient googleIdentityClient;
    @Mock AuthService authService;
    @InjectMocks GoogleOAuthService googleOAuthService;

    @Test
    @DisplayName("t1 확인된 Google 사용자 정보를 계정 인증에 전달한다")
    void t1_verifiedGoogleIdentityIsPassedToAuthentication() {
        when(googleIdentityClient.exchange("code", "http://localhost/callback"))
                .thenReturn(new GoogleIdentity("google-1", "USER@example.com", "민준", true));

        googleOAuthService.login("code", "http://localhost/callback");

        verify(authService).loginGoogle("google-1", "USER@example.com", "민준");
    }

    @Test
    @DisplayName("t2 확인되지 않은 Google 이메일은 인증을 거부한다")
    void t2_unverifiedGoogleEmailIsRejected() {
        when(googleIdentityClient.exchange("code", "http://localhost/callback"))
                .thenReturn(new GoogleIdentity("google-1", "user@example.com", "민준", false));

        assertThatThrownBy(() -> googleOAuthService.login("code", "http://localhost/callback"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Google에서 확인된 이메일이 필요합니다.");
        verifyNoInteractions(authService);
    }
}
