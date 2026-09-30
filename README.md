# 한끗

채용공고 탐색부터 부족 기술 학습과 지원 준비까지 연결하는 로컬 단일 사용자 취업 준비 앱입니다. 로그인 없이 한 설치 환경의 개인 데이터만 사용합니다.

현재는 개발용 로컬 실행을 기준으로 하며, 이후 다른 사용자에게 제공할 때도 중앙 사이트가 아니라 각자의 컴퓨터에서 실행되는 Docker Compose 기반 로컬 패키지를 우선합니다.

## 로컬 환경 준비

백엔드의 예시 환경 파일을 복사한 뒤 로컬 값으로 변경합니다. 실제 비밀값이 담긴 `.env`는 Git에서 제외됩니다.

```powershell
Set-Location backend
Copy-Item .env.example .env
docker compose up -d mysql
docker compose ps
```

## Backend

백엔드는 `backend/.env`를 자동으로 읽습니다.

```powershell
.\gradlew.bat bootRun
```

서버 상태 확인:

```text
GET http://localhost:8080/api/health
```

검증:

```powershell
.\gradlew.bat test
.\gradlew.bat clean build
```

## Frontend

```powershell
Set-Location frontend
npm install
npm run dev
```

검증:

```powershell
npm run lint
npm run build
```

## 로컬 인프라 종료

컨테이너만 종료하고 데이터 volume은 유지합니다.

```powershell
Set-Location backend
docker compose stop
```

데이터 volume 삭제는 저장된 로컬 데이터를 모두 제거하므로 명시적으로 초기화할 때만 수행합니다.
