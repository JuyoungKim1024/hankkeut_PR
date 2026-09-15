package back.backend.domain.auth.controller;

import back.backend.domain.auth.repository.RefreshTokenRepository;
import back.backend.domain.auth.service.AuthService;
import back.backend.global.exception.ApiException;
import back.backend.domain.user.entity.User;
import back.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthService authService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 유효한 정보로 가입하면 비밀번호를 암호화하고 token을 발급한다")
    void t1_signupHashesPasswordAndIssuesTokens() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"USER@example.com\",\"password\":\"password1\",\"name\":\"민준\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().httpOnly(AuthController.REFRESH_COOKIE, true));

        User saved = userRepository.findByEmail("user@example.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("password1");
        assertThat(passwordEncoder.matches("password1", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("t2 중복 이메일로 가입하면 409를 반환한다")
    void t2_duplicateEmailReturnsConflict() throws Exception {
        signupAndGetRefreshToken();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password1\",\"name\":\"다른 사용자\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("t3 잘못된 비밀번호로 로그인하면 401을 반환한다")
    void t3_wrongPasswordReturnsUnauthorized() throws Exception {
        signupAndGetRefreshToken();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"wrong-password1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("t4 refresh token을 재발급하면 이전 token은 다시 사용할 수 없다")
    void t4_refreshRotatesAndRejectsPreviousToken() throws Exception {
        String oldToken = signupAndGetRefreshToken();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new MockCookie(AuthController.REFRESH_COOKIE, oldToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String newToken = refreshResult.getResponse().getCookie(AuthController.REFRESH_COOKIE).getValue();
        assertThat(newToken).isNotEqualTo(oldToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new MockCookie(AuthController.REFRESH_COOKIE, oldToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("t5 LOCAL 계정과 같은 이메일의 Google 계정은 자동 병합하지 않는다")
    void t5_googleLoginDoesNotAutoMergeLocalAccount() throws Exception {
        signupAndGetRefreshToken();

        assertThatThrownBy(() -> authService.loginGoogle("google-1", "USER@example.com", "민준"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.code()).isEqualTo("OAUTH_EMAIL_CONFLICT"));
    }

    private String signupAndGetRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password1\",\"name\":\"민준\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return result.getResponse().getCookie(AuthController.REFRESH_COOKIE).getValue();
    }
}
