# Authentication Policy

## 일반 인증

- 비밀번호는 BCrypt로 단방향 hash하여 저장한다.
- Access token은 HS256 JWT이며 기본 수명은 15분이다.
- Refresh token은 예측 불가능한 임의 문자열이며 원문을 DB에 저장하지 않는다.
- Refresh token hash는 14일 동안 저장하고 재발급마다 rotation한다.
- 로그아웃 시 현재 refresh token을 폐기한다.
- Access token은 응답 body로 전달하고 프런트 메모리에서 관리한다.
- Refresh token은 `HttpOnly`, `SameSite=Lax` cookie로 전달한다. 운영 HTTPS에서는 `Secure`를 사용한다.

## 인증 오류

- 이메일 또는 비밀번호 불일치는 동일한 `INVALID_CREDENTIALS` 응답을 사용한다.
- 만료·변조된 Access token은 `401`이다.
- 권한 부족은 `403`이다.
- 폐기·만료·재사용된 Refresh token은 `INVALID_REFRESH_TOKEN`으로 처리한다.

## Google OAuth

- Google의 provider user id를 외부 계정 식별자로 사용한다.
- 이메일이 같은 LOCAL 계정과 자동 병합하지 않는다.
- 계정 연결 기능이 생기기 전까지 충돌 시 명시적인 오류를 반환한다.
- OAuth state와 redirect URI를 검증한다.

## 환경변수

```env
JWT_SECRET=
JWT_ACCESS_TOKEN_TTL=PT15M
JWT_REFRESH_TOKEN_TTL=P14D
AUTH_COOKIE_SECURE=false
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=http://localhost:8080/api/auth/kakao/callback
```

`JWT_SECRET`은 개발과 운영을 분리하고 최소 32바이트 이상의 임의값을 사용한다.
