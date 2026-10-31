<#
.SYNOPSIS
  Mengatur gangguan pada proxy inventory (Toxiproxy, compose profil chaos) untuk L10.1.
.EXAMPLE
  .\scripts\chaos.ps1 -Latency 3000              # setiap response inventory tertunda 3 s
  .\scripts\chaos.ps1 -Latency 3000 -Jitter 500  # 3 s +/- 0.5 s
  .\scripts\chaos.ps1 -Down                      # inventory tidak bisa dihubungi (connection refused)
  .\scripts\chaos.ps1 -Reset                     # normal kembali
  .\scripts\chaos.ps1                            # tampilkan status
#>
param(
    [int]$Latency = -1,
    [int]$Jitter = 0,
    [switch]$Down,
    [switch]$Reset
)

$ErrorActionPreference = 'Stop'
$api = 'http://localhost:8474/proxies/inventory'

# Toxiproxy menolak user agent browser (proteksi CSRF), termasuk default PowerShell.
function Invoke-Toxi([string]$Method = 'Get', [string]$Uri, [string]$Body) {
    $params = @{ Method = $Method; Uri = $Uri; UserAgent = 'tokoku-chaos' }
    if ($Body) { $params.ContentType = 'application/json'; $params.Body = $Body }
    Invoke-RestMethod @params
}

function Remove-AllToxics {
    $proxy = Invoke-Toxi -Uri $api
    foreach ($toxic in $proxy.toxics) {
        Invoke-Toxi -Method Delete -Uri "$api/toxics/$($toxic.name)" | Out-Null
    }
}

if ($Reset) {
    Remove-AllToxics
    Invoke-Toxi -Method Post -Uri $api -Body '{"enabled": true}' | Out-Null
    Write-Host '[OK] Proxy inventory normal (tanpa toxic, enabled).' -ForegroundColor Green
}
elseif ($Down) {
    Invoke-Toxi -Method Post -Uri $api -Body '{"enabled": false}' | Out-Null
    Write-Host '[OK] Proxy inventory DIMATIKAN: koneksi ke localhost:18083 ditolak.' -ForegroundColor Yellow
}
elseif ($Latency -ge 0) {
    Remove-AllToxics
    $body = @{ name = 'latency'; type = 'latency'; stream = 'downstream'; toxicity = 1.0;
               attributes = @{ latency = $Latency; jitter = $Jitter } } | ConvertTo-Json -Depth 3
    Invoke-Toxi -Method Post -Uri "$api/toxics" -Body $body | Out-Null
    Write-Host "[OK] Latency $Latency ms (jitter $Jitter ms) pada response inventory." -ForegroundColor Yellow
}

$proxy = Invoke-Toxi -Uri $api
$toxics = ($proxy.toxics | ForEach-Object { "$($_.type) $($_.attributes | ConvertTo-Json -Compress)" }) -join '; '
if (-not $toxics) { $toxics = '-' }
Write-Host "Status: enabled=$($proxy.enabled) listen=$($proxy.listen) upstream=$($proxy.upstream) toxics=$toxics"
