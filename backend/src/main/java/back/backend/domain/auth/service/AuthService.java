package back.backend.domain.auth.service;

import back.backend.domain.auth.dto.AuthResponse;
import back.backend.domain.auth.dto.LoginRequest;
import back.backend.domain.auth.dto.SignupRequest;
import back.backend.domain.user.entity.AuthProvider;
import back.backend.domain.user.entity.User;
import back.backend.domain.user.repository.UserRepository;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TokenResult signup(SignupRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다.");
        }
        User user = userRepository.save(User.createLocal(email, passwordEncoder.encode(request.password()), request.name().trim()));
        return tokens(user);
    }

    @Transactional
    public TokenResult login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .filter(found -> found.getProvider() == AuthProvider.LOCAL)
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
        return tokens(user);
    }

    @Transactional
    public TokenResult loginGoogle(String providerUserId, String emailValue, String name) {
        return userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                .map(this::tokens)
                .orElseGet(() -> {
                    String email = normalize(emailValue);
                    if (userRepository.existsByEmail(email)) {
                        throw new ApiException(HttpStatus.CONFLICT, "OAUTH_EMAIL_CONFLICT",
                                "같은 이메일의 기존 계정이 있습니다. 기존 로그인으로 인증해 주세요.");
                    }
                    User user = userRepository.save(User.createGoogle(email, name.trim(), providerUserId));
                    return tokens(user);
                });
    }

    @Transactional
    public TokenResult refresh(String refreshToken) {
        return tokens(refreshTokenService.consume(refreshToken));
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private TokenResult tokens(User user) {
        JwtService.AccessToken accessToken = jwtService.issue(user);
        String refreshToken = refreshTokenService.issue(user);
        AuthResponse response = new AuthResponse(user.getId(), user.getEmail(), user.getName(),
                accessToken.value(), accessToken.expiresInSeconds());
        return new TokenResult(response, refreshToken);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record TokenResult(AuthResponse response, String refreshToken) {
    }
}
