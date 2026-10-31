<#
.SYNOPSIS
  Mengisi data mode perf: 10.000 order alice/bob (infra/postgres/seed-perf.sql). Idempoten.
.DESCRIPTION
  Jalankan setelah order-service pernah start (tabel dibuat Flyway). Setelah reset-db.ps1, jalankan ulang
  order-service dulu, baru skrip ini.
.EXAMPLE
  .\scripts\seed-perf.ps1
#>
# Continue: docker menulis progres ke stderr, dan PowerShell 5.1 menganggapnya error bila output dialihkan.
# Kegagalan dicek lewat exit code.
$ErrorActionPreference = 'Continue'
$compose = Join-Path $PSScriptRoot '..\infra\compose.yaml'
$sql = Join-Path $PSScriptRoot '..\infra\postgres\seed-perf.sql'

Get-Content $sql -Raw | docker compose -f $compose exec -T postgres psql -v ON_ERROR_STOP=1 -q -U postgres -d tokoku
if ($LASTEXITCODE -ne 0) { throw 'Seed perf gagal. Sudahkah order-service start sekali (Flyway)?' }
Write-Host '[OK] Data perf terisi: 10.000 order (alice 60%, bob 40%), 3 item per order.' -ForegroundColor Green
