#Requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('Start', 'Stop', 'Update', 'Status')]
    [string]$Mode = 'Start',
    [ValidatePattern('^[a-z0-9][a-z0-9_-]*$')]
    [string]$ProjectName = 'hankkeut-local',
    [string]$StateDirectory,
    [ValidateRange(1024, 65535)]
    [int]$FrontendPort = 3000,
    [ValidateRange(1024, 65535)]
    [int]$BackendPort = 8080
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (!$StateDirectory) { $StateDirectory = Join-Path $root "backend/data/$ProjectName" }
$state = [IO.Path]::GetFullPath($StateDirectory)
$envFile = Join-Path $state '.env'
$resumes = Join-Path $state 'resumes'
$composeFile = Join-Path $root 'compose.local.yaml'
$compose = @('compose', '--project-name', $ProjectName, '--file', $composeFile, '--env-file', $envFile)

function Invoke-Compose([string[]]$Arguments) {
    & docker @compose @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose 실패 ($($Arguments[0])). Status와 컨테이너 로그를 확인하세요." }
}

function Initialize-Installation {
    if ($FrontendPort -eq $BackendPort) { throw '프런트와 백엔드 포트는 서로 달라야 합니다.' }
    $null = docker volume inspect "${ProjectName}_mysql-data" 2>$null
    if ($LASTEXITCODE -eq 0) { throw '기존 DB 볼륨이 있습니다. 이 설치의 .env를 복구하거나 새 프로젝트 이름을 지정하세요.' }
    New-Item -ItemType Directory -Force -Path $state | Out-Null
    $settings = @(
        "HANKKEUT_FRONTEND_PORT=$FrontendPort"
        "HANKKEUT_BACKEND_PORT=$BackendPort"
        "HANKKEUT_DB_PASSWORD=$([Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24)))"
        "HANKKEUT_DB_ROOT_PASSWORD=$([Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24)))"
    )
    $stream = [IO.File]::Open($envFile, [IO.FileMode]::CreateNew)
    $writer = [IO.StreamWriter]::new($stream)
    try { $writer.WriteLine(($settings -join [Environment]::NewLine)) }
    finally { $writer.Dispose() }
}

$null = docker info --format '{{.ServerVersion}}' 2>$null
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop을 실행한 뒤 다시 시도하세요.' }
if (!(Test-Path -LiteralPath $envFile -PathType Leaf)) {
    if ($Mode -ne 'Start') { throw 'Start로 로컬 설치를 먼저 준비하세요.' }
    Initialize-Installation
}

$previousResumeDirectory = $env:HANKKEUT_RESUME_DIR
$env:HANKKEUT_RESUME_DIR = $resumes
try {
    switch ($Mode) {
        'Start' {
            New-Item -ItemType Directory -Force -Path $resumes | Out-Null
            Invoke-Compose @('up', '--detach', '--build', '--wait', '--wait-timeout', '180')
        }
        'Stop' { Invoke-Compose @('stop') }
        'Status' { Invoke-Compose @('ps', '--all') }
        'Update' {
            $db = Invoke-Compose @('ps', '--quiet', 'mysql')
            if (!$db) { throw 'Update 전에 현재 설치를 Start로 실행하세요.' }
            Invoke-Compose @('build')
            Invoke-Compose @('stop', 'frontend', 'backend')
            try {
                $backupDirectory = Join-Path $state 'backups'
                New-Item -ItemType Directory -Force -Path $backupDirectory | Out-Null
                $backupPath = Join-Path $backupDirectory ("before-update-" + (Get-Date -Format 'yyyyMMdd-HHmmss-fff') + '.zip')
                & (Join-Path $PSScriptRoot 'LocalData.ps1') Backup -Archive $backupPath -SourceContainer $db -ResumeDirectory $resumes -BackendStopped | Out-Null
                Write-Host "업데이트 백업 $backupPath"
            } catch {
                Invoke-Compose @('start', 'backend', 'frontend')
                throw
            }
            Invoke-Compose @('up', '--detach', '--no-build', '--wait', '--wait-timeout', '180')
        }
    }
    if ($Mode -in @('Start', 'Update')) {
        $web = Invoke-Compose @('port', 'frontend', '3000')
        $api = Invoke-Compose @('port', 'backend', '8080')
        Write-Host "한끗 http://$web"
        Write-Host "API http://$api/api/health"
    }
} finally {
    $env:HANKKEUT_RESUME_DIR = $previousResumeDirectory
}
