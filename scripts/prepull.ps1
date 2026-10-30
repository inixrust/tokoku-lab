<#
.SYNOPSIS
  Mengunduh semua image Docker dan dependency Maven sebelum kelas (H-7), supaya kelas tidak menunggu unduhan.
.DESCRIPTION
  Bisa dijalankan dari checkpoint mana pun (termasuk L3.1-start): daftar unduhan mencakup kebutuhan SEMUA lab,
  bukan hanya checkpoint yang sedang aktif.
  1. docker pull image di scripts/images.txt (infra semua profil, base image JDK, Testcontainers, JMeter).
  2. Build image tokoku/wrk2 (wrk2 + wrk) dari loadtest/wrk2/Dockerfile.
  3. Maven: dependency:go-offline untuk semua dependency lab (scripts/prepull-deps/pom.xml) dan proyek saat ini.
  4. Bila checkpoint sudah punya mode perf (profil apps, mulai L12-perf): package + build image mode perf.
.EXAMPLE
  .\scripts\prepull.ps1
#>
# Continue: docker menulis progres ke stderr; kegagalan dicek lewat exit code.
$ErrorActionPreference = 'Continue'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$compose = Join-Path $root 'infra\compose.yaml'

function Invoke-Step([string]$title, [scriptblock]$action) {
    Write-Host "==> $title" -ForegroundColor Cyan
    & $action
    if ($LASTEXITCODE -ne 0) { throw "Gagal: $title" }
}

foreach ($image in Get-Content (Join-Path $PSScriptRoot 'images.txt') | Where-Object { $_ -and -not $_.StartsWith('#') -and -not $_.StartsWith('tokoku/') }) {
    Invoke-Step "docker pull $image" { docker pull $image }
}

Invoke-Step 'Build image tokoku/wrk2:1.0' { docker build -t tokoku/wrk2:1.0 (Join-Path $root 'loadtest\wrk2') }

Push-Location $root
try {
    Invoke-Step 'Maven dependency:go-offline (semua lab)' { .\mvnw.cmd -q -B -f scripts\prepull-deps\pom.xml dependency:go-offline }
    Invoke-Step 'Maven dependency:go-offline (checkpoint ini)' { .\mvnw.cmd -q -B dependency:go-offline }
    $profiles = docker compose -f $compose config --profiles
    if ($profiles -contains 'apps') {
        Invoke-Step 'Maven package (jar untuk image mode perf)' { .\mvnw.cmd -q -B package -DskipTests }
        Invoke-Step 'Build image mode perf (profil apps, aot)' {
            docker compose -f $compose --profile db --profile kafka --profile security --profile apps --profile aot build
        }
    }
} finally {
    Pop-Location
}

Write-Host '[OK] Semua unduhan selesai. Jalankan verify-env.ps1 untuk memastikan.' -ForegroundColor Green
