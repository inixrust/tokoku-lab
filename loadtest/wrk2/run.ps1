<#
.SYNOPSIS
  Menjalankan wrk2 (open model, laju konstan) lewat Docker image tokoku/wrk2.
.DESCRIPTION
  wrk2 tidak tersedia native di Windows, jadi dijalankan di container. Dari container, aplikasi di host
  dicapai lewat host.docker.internal (mode dev). Pada mode perf (Hari 5-6), pakai -Network tokoku_default
  dan nama service, mis. -Url http://order-service:8080/api/orders.
.EXAMPLE
  # L10.1: beban ringan 50 rps ke POST /api/orders selama 60 s
  .\loadtest\wrk2\run.ps1 -Rate 50 -Duration 60s -Url http://host.docker.internal:8082/api/orders -Script post-order.lua

  # L12.1: GET product dengan header versi
  .\loadtest\wrk2\run.ps1 -Rate 500 -Url http://host.docker.internal:8081/api/products/1 -Header 'API-Version: 1'

  # D12.1: wrk asli (closed model)
  .\loadtest\wrk2\run.ps1 -Closed -Url http://host.docker.internal:8090/stall
#>
param(
    [Parameter(Mandatory)] [string]$Url,
    [int]$Rate = 50,
    [string]$Duration = '60s',
    [int]$Connections = 20,
    [int]$Threads = 2,
    [string]$Script,
    [string[]]$Header = @(),
    [string]$Network,
    [string]$Token,
    [switch]$Closed
)

$ErrorActionPreference = 'Stop'
$image = 'tokoku/wrk2:1.0'

docker image inspect $image *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Image $image belum ada, build dulu..." -ForegroundColor Yellow
    docker build -t $image $PSScriptRoot
    if ($LASTEXITCODE -ne 0) { throw 'Build image wrk2 gagal.' }
}

$dockerArgs = @('run', '--rm', '-v', "${PSScriptRoot}:/scripts:ro")
if ($Network) { $dockerArgs += @('--network', $Network) }
if ($Closed) { $dockerArgs += @('--entrypoint', 'wrk') }
$dockerArgs += $image

$wrkArgs = @("-t$Threads", "-c$Connections", "-d$Duration", '--latency')
if (-not $Closed) { $wrkArgs += "-R$Rate" }
foreach ($h in $Header) { $wrkArgs += @('-H', $h) }
if ($Token) { $wrkArgs += @('-H', "Authorization: Bearer $Token") }
if ($Script) { $wrkArgs += @('-s', "/scripts/$Script") }
$wrkArgs += $Url

$mode = if ($Closed) { 'wrk (closed model)' } else { "wrk2 -R $Rate (open model)" }
Write-Host "==> $mode $Url selama $Duration" -ForegroundColor Cyan
& docker @dockerArgs @wrkArgs
