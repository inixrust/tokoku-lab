<#
.SYNOPSIS
  Mengosongkan database TokoKu (semua schema service) agar bisa lanjut dari checkpoint mana pun.
.EXAMPLE
  git checkout L5.1-solution
  .\scripts\reset-db.ps1
#>
$ErrorActionPreference = 'Stop'
$compose = Join-Path $PSScriptRoot '..\infra\compose.yaml'

docker compose -f $compose --profile db up -d --wait postgres
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL gagal dinyalakan.' }

docker compose -f $compose exec -T postgres psql -v ON_ERROR_STOP=1 -q -U postgres -d tokoku -f /tokoku/reset.sql
if ($LASTEXITCODE -ne 0) { throw 'Reset database gagal.' }

Write-Host '[OK] Schema product, orders, inventory dibuat ulang. Jalankan ulang service; Flyway akan memigrasi dari awal.' -ForegroundColor Green
