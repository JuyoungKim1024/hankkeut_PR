# External Services

외부 서비스의 실제 연동이 필요한 시점과 사용자가 준비할 항목을 정리합니다. 비밀값은 이 문서나 채팅에 기록하지 않고 로컬 `.env` 또는 배포 환경의 secret 설정에만 저장합니다.

## 공통 원칙

- 구현과 테스트는 먼저 fixture와 stub으로 진행합니다.
- 실제 연동 확인 직전에 필요한 서비스만 요청합니다.
- 최소 권한의 개발용 credential을 사용합니다.
- 운영 credential과 개발 credential을 분리합니다.
- credential 값을 채팅, Git, 로그, fixture에 남기지 않습니다.

## 사람인 Open API

필요 시점:

- `016 사람인 API 계약 어댑터`는 fixture로 먼저 구현할 수 있습니다.
- 실제 응답 확인과 `017 공고 수집` live smoke test 전에 필요합니다.

준비 항목:

- 사람인 Open API 사용 신청과 승인
- 발급된 access key
- 허용된 API 기능과 일일 호출 제한 확인
- 서비스 이용약관에 따른 저장·노출 가능 필드 확인

환경변수 예정:

```env
SARAMIN_ACCESS_KEY=
```

## OpenAI API

필요 시점:

- `020 OpenAI Gateway`의 stub·schema 테스트 후 실제 구조화 출력 smoke test 전에 필요합니다.

준비 항목:

- 개발용 OpenAI API project
- 제한된 개발용 API key
- 사용할 model과 project 사용량 한도

환경변수 예정:

```env
OPENAI_API_KEY=
OPENAI_PROJECT_ID=
OPENAI_MODEL=
```

실제 model은 연동 시점의 공식 지원 상태와 비용을 확인한 뒤 확정합니다.

## Google OAuth

필요 시점:

- `013 Google OAuth 로그인`의 redirect와 사용자 정보 stub 테스트 후 실제 로그인 확인 전에 필요합니다.

준비 항목:

- Google Cloud project
- OAuth consent screen 설정
- Web application OAuth client
- 로컬 및 배포 redirect URI 등록

환경변수 예정:

```env
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

## Cloudflare R2

필요 시점:

- `014 이력서 파일 저장`의 storage stub과 파일 검증 구현 후 실제 업로드 smoke test 전에 필요합니다.

준비 항목:

- 개발용 R2 bucket
- bucket 범위로 제한한 access key ID와 secret access key
- account ID
- S3-compatible endpoint
- 파일 공개 여부와 CORS 정책 결정

환경변수 예정:

```env
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET_NAME=
R2_ENDPOINT=
```

## 배포 서비스

### Vercel

프런트 배포 시 GitHub 저장소 연결과 환경변수 등록이 필요합니다. CLI 자동 배포가 필요하지 않다면 별도 API token은 요구하지 않습니다.

### Backend hosting

백엔드 배포 대상이 확정되지 않았습니다. 실제 배포 작업 전에 실행 시간, Scheduler 지속 실행, MySQL 연결, 비용 조건을 비교하고 필요한 credential을 별도로 정합니다.

## 현재 준비할 필요가 없는 항목

- Redis: 현재 MVP 설계에서 제외
- Wanted API: 현재 MVP 수집 범위에서 제외
- Prometheus/Grafana: 초기 MVP에서 제외
