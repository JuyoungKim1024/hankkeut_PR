# 한끗

채용공고 탐색부터 부족 기술 학습과 지원 준비까지 연결하는 취업 준비 서비스입니다.

## 로컬 환경 준비

루트의 예시 환경 파일을 복사한 뒤 로컬 값으로 변경합니다. 실제 비밀값이 담긴 `.env`는 Git에서 제외됩니다.

```powershell
Copy-Item .env.example .env
docker compose up -d mysql
docker compose ps
```

## Backend

PowerShell 세션에 `.env`의 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 설정한 뒤 실행합니다.

```powershell
Set-Location backend
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
docker compose stop
```

데이터 volume 삭제는 저장된 로컬 데이터를 모두 제거하므로 명시적으로 초기화할 때만 수행합니다.
