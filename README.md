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

URL 가져오기를 사용하려면 약관과 robots 지침이 해당 수집을 허용하는 공개 채용공고 호스트를 확인한 뒤 `JOB_CRAWLING_ALLOWED_HOSTS`에 쉼표로 구분해 설정합니다. 빈 값에서는 공고 추가 화면의 직접 입력을 이용합니다.

```env
JOB_CRAWLING_ALLOWED_HOSTS=
JOB_CRAWLING_REFRESH_INTERVAL=PT6H
JOB_CRAWLING_MINIMUM_HOST_INTERVAL=PT2S
```

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

## 로컬 데이터 백업과 복원

Docker와 PowerShell 7.4 이상이 필요합니다. 백엔드를 종료하고 원본 MySQL 컨테이너는 실행한 상태에서 저장소 루트에서 백업합니다. 이력서 저장 경로를 바꿨다면 실제 경로를 지정하세요.

```powershell
& .\scripts\LocalData.ps1 Backup -Archive .\backend\data\hankkeut-backup.zip -ResumeDirectory .\backend\data\resumes -BackendStopped
& .\scripts\LocalData.ps1 Verify -Archive .\backend\data\hankkeut-backup.zip
```

출력 ZIP의 상위 폴더를 먼저 준비하세요. 기본 원본 컨테이너는 `hankkeut-mysql`입니다. 다른 한끗 MySQL 컨테이너를 사용하는 경우 `-SourceContainer`로 지정합니다. ZIP에는 DB 데이터와 원본 이력서가 들어 있으므로 사용자 데이터로 보관하세요.

```powershell
$restored = & .\scripts\LocalData.ps1 Restore -Archive .\backend\data\hankkeut-backup.zip -RestoreDirectory .\backend\data\restored
$jar = (Resolve-Path .\backend\build\libs\backend-0.0.1-SNAPSHOT.jar).Path
Set-Location $restored.directory
java -jar $jar
```

복원은 아직 존재하지 않는 폴더와 루프백 포트의 별도 MySQL 컨테이너를 생성합니다. 위 명령은 빌드된 JAR를 복원 폴더의 설정과 파일로 실행합니다. 기본 백엔드 포트를 쓰는 다른 앱은 먼저 종료하거나 별도 포트를 지정하세요. 복원한 MySQL은 `docker stop $restored.container`로 중지하고 `docker start $restored.container`로 다시 시작할 수 있습니다.

Verify는 ZIP 명세, 스키마 버전, 항목 크기와 SHA256을 검사합니다. 소유한 백업의 복원에 사용하세요. DB 계정 설정과 API 키는 백업 파일에 포함되지 않습니다.

## 로컬 인프라 종료

컨테이너만 종료하고 데이터 volume은 유지합니다.

```powershell
Set-Location backend
docker compose stop
```

데이터 volume 삭제는 저장된 로컬 데이터를 모두 제거하므로 명시적으로 초기화할 때만 수행합니다.
