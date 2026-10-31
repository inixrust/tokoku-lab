<#
.SYNOPSIS
  L13.1 langkah 4: startup product-service di JDK 25 tanpa vs dengan AOT cache (slide 120).
.DESCRIPTION
  1. Build image product-service JDK 25 (jar sudah di-extract di Dockerfile mode perf).
  2. Training run: -XX:AOTCacheOutput=/aot/app.aot + -Dspring.context.exit=onRefresh (keluar setelah context refresh).
  3. Ukur startup N kali tanpa cache dan N kali dengan -XX:AOTCache=/aot/app.aot (dari log "Started ... in X seconds").
  Butuh infra profil db (dan kafka/obs bila ingin metric/trace tidak error).
.EXAMPLE
  .\scripts\aot-cache.ps1 -Runs 3
#>
param([int]$Runs = 3)

# Continue: docker menulis progres/log ke stderr, dan PowerShell 5.1 menganggapnya error bila dialihkan.
# Kegagalan tetap dihentikan lewat throw berdasarkan exit code.
$ErrorActionPreference = 'Continue'
$compose = Join-Path $PSScriptRoot '..\infra\compose.yaml'
$base = @('compose', '-f', $compose, '--profile', 'db', '--profile', 'aot')

function Invoke-Compose([string[]]$extra) {
    & docker @base @extra
    if ($LASTEXITCODE -ne 0) { throw "docker compose $($extra -join ' ') gagal" }
}

function Measure-Startup([string]$javaOpts) {
    $name = "tokoku-aot-$([guid]::NewGuid().ToString('N').Substring(0, 8))"
    Invoke-Compose @('run', '-d', '--name', $name, '-e', "JAVA_TOOL_OPTIONS=$javaOpts", 'product-service-jdk25') | Out-Null
    try {
        for ($i = 0; $i -lt 120; $i++) {
            $log = docker logs $name 2>&1 | Out-String
            if ($log -match 'Started \w+ in ([\d.]+) seconds') { return [double]$Matches[1] }
            Start-Sleep -Milliseconds 500
        }
        throw "Startup tidak selesai: $name"
    } finally {
        docker rm -f $name *> $null
    }
}

Write-Host '==> Build image JDK 25' -ForegroundColor Cyan
Invoke-Compose @('build', 'product-service-jdk25')

Write-Host '==> Training run (membuat /aot/app.aot)' -ForegroundColor Cyan
Invoke-Compose @('run', '--rm', '-e', 'JAVA_TOOL_OPTIONS=-XX:AOTCacheOutput=/aot/app.aot -Dspring.context.exit=onRefresh',
    'product-service-jdk25') | Select-String -Pattern 'AOTCache|aot|error' | ForEach-Object { $_.Line }

$results = foreach ($mode in 'tanpa cache', 'dengan AOT cache') {
    $opts = if ($mode -eq 'tanpa cache') { '' } else { '-XX:AOTCache=/aot/app.aot' }
    foreach ($run in 1..$Runs) {
        $seconds = Measure-Startup $opts
        Write-Host ("{0,-17} run {1}: {2:N3} s" -f $mode, $run, $seconds)
        [pscustomobject]@{ Mode = $mode; Run = $run; Seconds = $seconds }
    }
}

Write-Host ''
$results | Group-Object Mode | ForEach-Object {
    $stats = $_.Group | Measure-Object Seconds -Average -Minimum -Maximum
    '{0,-17} rata-rata {1:N3} s (min {2:N3}, maks {3:N3})' -f $_.Name, $stats.Average, $stats.Minimum, $stats.Maximum
}
