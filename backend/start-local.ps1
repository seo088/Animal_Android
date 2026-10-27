param(
    [string]$FusekiJar = (Join-Path $env:USERPROFILE 'Downloads\apache-jena-fuseki-6.1.0\fuseki-server.jar'),
    [string]$Java = 'java',
    [switch]$EnablePublicSync
)

$ErrorActionPreference = 'Stop'
$backendRoot = $PSScriptRoot
$python = Join-Path $backendRoot '.venv\Scripts\python.exe'
$dataset = Join-Path $backendRoot 'fuseki-data'
$runtime = Join-Path $backendRoot '.runtime'

foreach ($required in @($python, $FusekiJar, (Join-Path $backendRoot '.env'))) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Required existing file is missing: $required"
    }
}
if (-not (Test-Path -LiteralPath $dataset -PathType Container) -or
    -not (Get-ChildItem -LiteralPath $dataset -Directory -Filter 'Data-*')) {
    throw 'Existing Fuseki data was not found. No new dataset will be created.'
}

function Wait-Ready([string]$Url, [string]$Kind) {
    for ($attempt = 0; $attempt -lt 35; $attempt++) {
        try {
            $result = Invoke-RestMethod -Uri $Url -TimeoutSec 2
            if ($Kind -eq 'api' -and $result.info.title -ne 'AnimalLoo API') {
                throw 'Port 8000 belongs to a different application.'
            }
            if ($Kind -eq 'fuseki' -and $result.boolean -ne $true) {
                throw 'The existing diagnosis dataset is empty or unavailable.'
            }
            return
        } catch {
            if ($attempt -eq 34) { throw "Readiness check failed for $Kind. See $runtime logs. $($_.Exception.Message)" }
            Start-Sleep -Milliseconds 500
        }
    }
}

New-Item -ItemType Directory -Path $runtime -Force | Out-Null
$started = @()
$previousSync = $env:SYNC_INTERVAL_MINUTES
try {
    if (-not (Get-NetTCPConnection -LocalPort 3030 -State Listen -ErrorAction SilentlyContinue)) {
        $process = Start-Process -FilePath $Java -ArgumentList @(
            '-jar', ('"{0}"' -f $FusekiJar), '--localhost', '--tdb2',
            ('--loc="{0}"' -f $dataset), '/animalloo'
        ) -WorkingDirectory $backendRoot -WindowStyle Hidden -PassThru `
          -RedirectStandardOutput (Join-Path $runtime 'fuseki.stdout.log') `
          -RedirectStandardError (Join-Path $runtime 'fuseki.stderr.log')
        $started += $process
    }
    Wait-Ready 'http://127.0.0.1:3030/animalloo/query?query=ASK%20%7B%3Fs%20%3Fp%20%3Fo%7D&format=json' 'fuseki'

    if (-not (Get-NetTCPConnection -LocalPort 8000 -State Listen -ErrorAction SilentlyContinue)) {
        if (-not $EnablePublicSync) { $env:SYNC_INTERVAL_MINUTES = '0' }
        $process = Start-Process -FilePath $python -ArgumentList @(
            '-m', 'uvicorn', 'app.main:app', '--host', '127.0.0.1', '--port', '8000'
        ) -WorkingDirectory $backendRoot -WindowStyle Hidden -PassThru `
          -RedirectStandardOutput (Join-Path $runtime 'api.stdout.log') `
          -RedirectStandardError (Join-Path $runtime 'api.stderr.log')
        $started += $process
    }
    Wait-Ready 'http://127.0.0.1:8000/openapi.json' 'api'
    $health = Invoke-RestMethod 'http://127.0.0.1:8000/health' -TimeoutSec 8
    if ($health.database -ne 'ok' -or $health.fuseki -ne 'ok') {
        throw "Backend dependency check failed: $($health | ConvertTo-Json -Compress)"
    }
    Write-Output 'AnimalLoo ready: API 127.0.0.1:8000, diagnosis 127.0.0.1:3030.'
    Write-Output 'Android emulator API_BASE_URL=http://10.0.2.2:8000/; USE_MOCK_API=false.'
    Write-Output 'Existing DB settings and stored data are retained. Logs: backend/.runtime/'
} catch {
    # Only stop processes started by this invocation; never stop an existing service.
    foreach ($process in $started) {
        if (-not $process.HasExited) { $process.Kill() }
    }
    throw
} finally {
    $env:SYNC_INTERVAL_MINUTES = $previousSync
}
