<#
.SYNOPSIS
  Mengirim ulang event OrderPlaced untuk menguji consumer idempotent (L7.1).
.DESCRIPTION
  Mengosongkan published_at pada baris outbox order-service, sehingga OutboxRelay mengirim event yang SAMA
  (eventId sama) sekali lagi. Inilah yang terjadi di produksi bila relay crash setelah kirim, sebelum menandai
  terkirim (at-least-once delivery).
.PARAMETER OrderId
  Order yang event-nya dikirim ulang. Tanpa parameter: event OrderPlaced terakhir.
.EXAMPLE
  .\scripts\replay-event.ps1
  .\scripts\replay-event.ps1 -OrderId 1001
#>
param([long]$OrderId = 0)

$ErrorActionPreference = 'Stop'
$compose = Join-Path $PSScriptRoot '..\infra\compose.yaml'

$filter = "event_type = 'OrderPlaced'"
if ($OrderId -gt 0) { $filter += " and aggregate_id = '$OrderId'" }

$sql = "update orders.outbox set published_at = null where id = (select max(id) from orders.outbox where $filter) returning aggregate_id, event_id;"
$result = docker compose -f $compose exec -T postgres psql -U postgres -d tokoku -t -A -c $sql
if ($LASTEXITCODE -ne 0) { throw 'Gagal mengakses outbox order-service.' }

$row = ($result | Where-Object { $_ -match '\|' } | Select-Object -First 1)
if (-not $row) {
    Write-Host '[!] Tidak ada event OrderPlaced yang cocok di outbox.' -ForegroundColor Yellow
    exit 1
}
$parts = $row.Split('|')
Write-Host "[OK] OrderPlaced untuk order $($parts[0]) (eventId $($parts[1])) akan dikirim ulang oleh relay." -ForegroundColor Green
Write-Host '     Cek stok product terkait: tidak boleh berkurang dua kali.'
