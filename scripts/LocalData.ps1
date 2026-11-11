#Requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Mandatory, Position = 0)]
    [ValidateSet('Backup', 'Verify', 'Restore')]
    [string]$Mode,
    [Parameter(Mandatory)]
    [string]$Archive,
    [string]$SourceContainer = 'hankkeut-mysql',
    [string]$ResumeDirectory,
    [string]$RestoreDirectory,
    [switch]$BackendStopped
)

$ErrorActionPreference = 'Stop'
$archivePath = [IO.Path]::GetFullPath($Archive)
$migrationDirectory = Join-Path $PSScriptRoot '../backend/src/main/resources/db/migration'
$latestSchema = (Get-ChildItem -LiteralPath $migrationDirectory -File | ForEach-Object {
    if ($_.Name -match '^V(\d+)__') { [int]$Matches[1] }
} | Measure-Object -Maximum).Maximum
$maxArchiveBytes = 2GB
$keyPattern = '[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}\.(?:pdf|docx)'
$schemaQuery = "select version from flyway_schema_history where success = 1 and version is not null order by installed_rank desc limit 1"
$resumeQuery = "select distinct file_url from resume order by file_url"
$mysql = 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --protocol=TCP --host=127.0.0.1 --user="$MYSQL_USER" --database="$MYSQL_DATABASE" --batch --skip-column-names --raw --binary-mode --local-infile=0 "$@"'

function Invoke-Query([string]$Container, [string]$Query) {
    $value = docker exec $Container sh -c $mysql mysql --execute=$Query
    if ($LASTEXITCODE -ne 0) { throw 'MySQL 조회 실패' }
    return $value
}

function Get-ResumeKeys([string]$Container) {
    $keys = @(Invoke-Query $Container $resumeQuery)
    foreach ($key in $keys) {
        if ($key -cnotmatch "^$keyPattern$") { throw '지원하지 않는 이력서 저장 키가 있습니다.' }
    }
    return $keys
}

function Get-EntryLimit([string]$Name) {
    if ($Name -ceq 'database.sql') { return 256MB }
    if ($Name -ceq 'manifest.json') { return 512KB }
    if ($Name -cmatch "^resumes/$keyPattern$") { return 10MB }
    throw "지원하지 않는 백업 항목: $Name"
}

function Copy-CheckedEntry($Entry, [IO.Stream]$OutputStream) {
    $max = Get-EntryLimit $Entry.FullName
    if ($Entry.Length -lt 1 -or $Entry.Length -gt $max) { throw "백업 항목 크기 범위 초과: $($Entry.FullName)" }
    $inputStream = $Entry.Open()
    $hasher = [Security.Cryptography.IncrementalHash]::CreateHash([Security.Cryptography.HashAlgorithmName]::SHA256)
    try {
        $buffer = [byte[]]::new(65536)
        $readBytes = 0L
        while (($count = $inputStream.Read($buffer, 0, $buffer.Length)) -gt 0) {
            $readBytes += $count
            if ($readBytes -gt $Entry.Length) { throw "백업 항목 길이 불일치: $($Entry.FullName)" }
            $hasher.AppendData($buffer, 0, $count)
            if ($OutputStream) { $OutputStream.Write($buffer, 0, $count) }
        }
        if ($readBytes -ne $Entry.Length) { throw "백업 항목 길이 불일치: $($Entry.FullName)" }
        return [Convert]::ToHexString($hasher.GetHashAndReset())
    } finally {
        $hasher.Dispose()
        $inputStream.Dispose()
    }
}

function Read-Backup($Zip) {
    $entries = [Collections.Generic.Dictionary[string, IO.Compression.ZipArchiveEntry]]::new([StringComparer]::Ordinal)
    $total = 0L
    foreach ($entry in $Zip.Entries) {
        $limit = Get-EntryLimit $entry.FullName
        if ($entry.Length -lt 1 -or $entry.Length -gt $limit -or !$entries.TryAdd($entry.FullName, $entry)) {
            throw '잘못된 크기 또는 중복 백업 항목'
        }
        $total += $entry.Length
        if ($total -gt $maxArchiveBytes) { throw '백업 압축 해제 크기가 2GiB를 초과합니다.' }
    }
    if (!$entries.ContainsKey('manifest.json') -or !$entries.ContainsKey('database.sql')) {
        throw '백업 명세 또는 데이터베이스 파일이 없습니다.'
    }
    $buffer = [IO.MemoryStream]::new()
    try {
        $null = Copy-CheckedEntry $entries['manifest.json'] $buffer
        $manifest = [Text.Encoding]::UTF8.GetString($buffer.ToArray()) | ConvertFrom-Json
    } finally { $buffer.Dispose() }
    if ($manifest.format -cne 'hankkeut-local-data' -or $manifest.formatVersion -ne 1 -or
        $manifest.schemaVersion -notmatch '^\d+$' -or [int]$manifest.schemaVersion -lt 6 -or
        [int]$manifest.schemaVersion -gt $latestSchema) {
        throw '지원하지 않는 백업 형식 또는 DB 스키마 버전'
    }
    $verified = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($file in @($manifest.files)) {
        $name = [string]$file.name
        if ($name -ceq 'manifest.json' -or !$entries.ContainsKey($name) -or !$verified.Add($name) -or
            $file.bytes -ne $entries[$name].Length -or $file.sha256 -notmatch '^[a-fA-F0-9]{64}$') {
            throw '백업 명세와 항목이 일치하지 않습니다.'
        }
        if ((Copy-CheckedEntry $entries[$name] $null) -ne $file.sha256) {
            throw "SHA256 불일치: $name"
        }
    }
    if ($verified.Count -ne $entries.Count - 1 -or !$verified.Contains('database.sql')) {
        throw '백업에 명세가 없거나 누락된 항목이 있습니다.'
    }
    return @{ manifest = $manifest; entries = $entries }
}

function New-Backup {
    if (!$BackendStopped) { throw '백엔드를 종료한 뒤 -BackendStopped를 지정하세요.' }
    if (!$ResumeDirectory) { throw '실제 이력서 저장 경로를 -ResumeDirectory로 지정하세요.' }
    if ([IO.File]::Exists($archivePath)) { throw '기존 백업을 덮어쓸 수 없습니다.' }
    $resumeRoot = [IO.Path]::GetFullPath($ResumeDirectory)
    $schema = [string](Invoke-Query $SourceContainer $schemaQuery)
    if ($schema -notmatch '^\d+$' -or [int]$schema -lt 6 -or [int]$schema -gt $latestSchema) {
        throw '지원하지 않는 원본 DB 스키마 버전'
    }
    $resumeKeys = @(Get-ResumeKeys $SourceContainer)
    $sqlPath = Join-Path ([IO.Path]::GetTempPath()) ("hankkeut-" + [guid]::NewGuid().ToString('N') + '.sql')
    $partial = "$archivePath.partial-$([guid]::NewGuid().ToString('N'))"
    try {
        $dump = 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysqldump --protocol=TCP --host=127.0.0.1 --user="$MYSQL_USER" --single-transaction --skip-lock-tables --no-tablespaces --set-gtid-purged=OFF --skip-add-drop-table --skip-triggers -- "$MYSQL_DATABASE"'
        docker exec $SourceContainer sh -c $dump > $sqlPath 2>$null
        if ($LASTEXITCODE -ne 0) { throw 'MySQL 백업 실패' }
        if (Compare-Object -ReferenceObject (@('') + $resumeKeys) -DifferenceObject (@('') + @(Get-ResumeKeys $SourceContainer))) {
            throw '백업 중 이력서 데이터가 변경됐습니다.'
        }
        $files = [ordered]@{'database.sql' = $sqlPath}
        foreach ($key in $resumeKeys) {
            $files["resumes/$key"] = Join-Path $resumeRoot $key
        }
        $zip = [IO.Compression.ZipFile]::Open($partial, [IO.Compression.ZipArchiveMode]::Create)
        try {
            $records = foreach ($entry in $files.GetEnumerator()) {
                $file = Get-Item -LiteralPath $entry.Value
                if ($file.PSIsContainer -or ($file.Attributes -band [IO.FileAttributes]::ReparsePoint) -or
                    $file.Length -lt 1 -or $file.Length -gt (Get-EntryLimit $entry.Key)) {
                    throw "백업할 파일이 유효하지 않습니다: $($entry.Key)"
                }
                $digest = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
                [void][IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $file.FullName, $entry.Key)
                @{name=$entry.Key;bytes=$file.Length;sha256=$digest}
            }
            $metadata = @{format='hankkeut-local-data';formatVersion=1;schemaVersion=$schema;files=@($records)}
            $writer = [IO.StreamWriter]::new($zip.CreateEntry('manifest.json').Open())
            try { $writer.Write(($metadata | ConvertTo-Json -Depth 5)) } finally { $writer.Dispose() }
        } finally { $zip.Dispose() }
        $verify = [IO.Compression.ZipFile]::OpenRead($partial)
        try { $null = Read-Backup $verify } finally { $verify.Dispose() }
        [IO.File]::Move($partial, $archivePath)
        return [pscustomobject]@{archive=$archivePath;schemaVersion=$schema;resumeCount=$resumeKeys.Count}
    } finally {
        foreach ($path in @($sqlPath,$partial)) {
            if ([IO.File]::Exists($path)) { [IO.File]::Delete($path) }
        }
    }
}

function Import-Database([string]$Container, $Entry) {
    $process = [Diagnostics.Process]::new()
    $process.StartInfo.FileName = Get-Command docker.exe -CommandType Application | Select-Object -First 1 -ExpandProperty Source
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.RedirectStandardInput = $true
    $process.StartInfo.RedirectStandardError = $true
    foreach ($argument in @('exec','-i',$Container,'sh','-c',$mysql,'mysql')) {
        $process.StartInfo.ArgumentList.Add($argument)
    }
    try {
        [void]$process.Start()
        $stderrDrain = $process.StandardError.BaseStream.CopyToAsync([IO.Stream]::Null)
        try { $null = Copy-CheckedEntry $Entry $process.StandardInput.BaseStream }
        finally { $process.StandardInput.Close() }
        if (!$process.WaitForExit(120000)) {
            $process.Kill()
            throw 'MySQL 복원 시간 초과'
        }
        $stderrDrain.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) { throw 'MySQL 복원 실패' }
    } finally { $process.Dispose() }
}

function Restore-Backup($Verified) {
    if (!$RestoreDirectory) { throw '새 폴더를 -RestoreDirectory로 지정하세요.' }
    $target = [IO.Path]::GetFullPath($RestoreDirectory)
    if (Test-Path -LiteralPath $target) { throw '복원에는 아직 존재하지 않는 경로를 지정하세요.' }
    $name = 'hankkeut-restored-' + [guid]::NewGuid().ToString('N').Substring(0,12)
    $password = [guid]::NewGuid().ToString('N')
    $oldPassword = $env:MYSQL_PASSWORD
    $started = $false
    $stage = $null
    $writtenFiles = [Collections.Generic.List[string]]::new()
    try {
        $env:MYSQL_PASSWORD = $password
        $null = docker run -d --name $name --publish '127.0.0.1::3306' -e MYSQL_RANDOM_ROOT_PASSWORD=yes `
            -e MYSQL_DATABASE=hankkeut -e MYSQL_USER=hankkeut -e MYSQL_PASSWORD -e TZ=UTC mysql:8.4
        if ($LASTEXITCODE -ne 0) { throw '복원용 MySQL 시작 실패' }
        $started = $true
        $binding = docker port $name 3306/tcp
        if ($LASTEXITCODE -ne 0 -or $binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw '복원용 DB 포트 조회 실패' }
        $port = $Matches[1]
        $ready = $false
        for ($attempt = 0; $attempt -lt 45; $attempt++) {
            docker exec $name sh -c $mysql mysql --execute='select 1' *> $null
            if ($LASTEXITCODE -eq 0) { $ready = $true; break }
            Start-Sleep -Seconds 1
        }
        if (!$ready) { throw '복원용 MySQL 준비 시간 초과' }
        Import-Database $name $Verified.entries['database.sql']
        if ((Invoke-Query $name $schemaQuery) -ne $Verified.manifest.schemaVersion) {
            throw '복원 DB의 스키마가 백업 명세와 다릅니다.'
        }
        $keys = @(Get-ResumeKeys $name)
        $expected = @($keys | ForEach-Object { "resumes/$_" })
        $archived = @($Verified.entries.Keys | Where-Object { $_.StartsWith('resumes/') })
        if (Compare-Object -ReferenceObject (@('') + $expected) -DifferenceObject (@('') + $archived) -CaseSensitive) {
            throw '복원 DB의 이력서와 백업 파일 목록이 다릅니다.'
        }
        $stage = "$target.partial-$([guid]::NewGuid().ToString('N'))"
        New-Item -ItemType Directory -Path (Join-Path $stage 'resumes') | Out-Null
        foreach ($path in $expected) {
            $output = Join-Path $stage $path
            $file = [IO.File]::Open($output, [IO.FileMode]::CreateNew)
            $writtenFiles.Add($output)
            try { $null = Copy-CheckedEntry $Verified.entries[$path] $file } finally { $file.Dispose() }
        }
        $envPath = Join-Path $stage '.env'
        $writtenFiles.Add($envPath)
        @(
            "DB_URL=jdbc:mysql://127.0.0.1:$port/hankkeut?serverTimezone=UTC&characterEncoding=UTF-8"
            'DB_USERNAME=hankkeut'
            "DB_PASSWORD=$password"
            'RESUME_STORAGE_PATH=./resumes'
        ) | Set-Content -LiteralPath $envPath -Encoding utf8
        [IO.Directory]::Move($stage, $target)
        return [pscustomobject]@{container=$name;directory=$target;port=[int]$port;resumeCount=$keys.Count}
    } catch {
        if ($started) { docker rm -f -v $name | Out-Null }
        if ($stage -and [IO.Directory]::Exists($stage)) {
            try {
                foreach ($file in $writtenFiles) { [IO.File]::Delete($file) }
                $resumeStage = Join-Path $stage 'resumes'
                if ([IO.Directory]::Exists($resumeStage)) { [IO.Directory]::Delete($resumeStage) }
                [IO.Directory]::Delete($stage)
            } catch {
                Write-Warning "부분 복원 폴더를 정리하지 못했습니다: $stage"
            }
        }
        throw
    } finally { $env:MYSQL_PASSWORD = $oldPassword }
}

if ($Mode -eq 'Backup') {
    New-Backup
} else {
    $archiveFile = Get-Item -LiteralPath $archivePath
    if ($archiveFile.PSIsContainer -or $archiveFile.Length -gt $maxArchiveBytes) { throw '잘못된 백업 파일 크기' }
    $zip = [IO.Compression.ZipFile]::OpenRead($archivePath)
    try {
        $verified = Read-Backup $zip
        if ($Mode -eq 'Verify') {
            [pscustomobject]@{archive=$archivePath;schemaVersion=$verified.manifest.schemaVersion;valid=$true}
        } else {
            Restore-Backup $verified
        }
    } finally { $zip.Dispose() }
}
