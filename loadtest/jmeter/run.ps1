<#
.SYNOPSIS
  Menjalankan payments.jmx di CLI (non-GUI) dan membuat HTML report (slide 113).
.DESCRIPTION
  Schedule versi kelas (rencana L12.2): ramp-up 30 s, steady 3 menit, ramp-down 30 s. Schedule slide 112
  (2 + 10 + 1 menit) adalah durasi uji sungguhan: -RampUp 120 -Steady 600 -RampDown 60.
  Hasil: loadtest/results/payments-<waktu>/results.jtl dan report/index.html.

  Tanpa JMeter lokal (atau dengan -Docker): memakai image alpine/jmeter:5.6.3 di jaringan compose tokoku_default,
  sehingga target memakai nama service (order-service:8080, keycloak:8080).
.EXAMPLE
  .\loadtest\jmeter\run.ps1 -Rps 200
  .\loadtest\jmeter\run.ps1 -Rps 200 -Docker
#>
param(
    [int]$Rps = 200,
    [int]$RampUp = 30,
    [int]$Steady = 180,
    [int]$RampDown = 30,
    [switch]$Docker
)

$ErrorActionPreference = 'Continue'
$loadtest = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$name = "payments-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Force (Join-Path $loadtest "results\$name") | Out-Null
$schedule = @("-Jrps=$Rps", "-Jrampup=$RampUp", "-Jsteady=$Steady", "-Jrampdown=$RampDown")

$jmeter = if (Get-Command jmeter -ErrorAction SilentlyContinue) { 'jmeter' }
          elseif ($env:JMETER_HOME) { Join-Path $env:JMETER_HOME 'bin\jmeter.bat' }
if (-not $jmeter) { $Docker = $true }

Write-Host "==> JMeter open model: $Rps rps (ramp $RampUp s, steady $Steady s, ramp-down $RampDown s)" -ForegroundColor Cyan
if ($Docker) {
    docker run --rm --network tokoku_default -v "${loadtest}:/loadtest" -w /loadtest/jmeter alpine/jmeter:5.6.3 `
        -n -t /loadtest/jmeter/payments.jmx -q /loadtest/jmeter/user.properties `
        -l "/loadtest/results/$name/results.jtl" -e -o "/loadtest/results/$name/report" `
        @schedule -Jhost=order-service -Jport=8080 -JkcHost=keycloak -JkcPort=8080
} else {
    & $jmeter -n -t (Join-Path $PSScriptRoot 'payments.jmx') -q (Join-Path $PSScriptRoot 'user.properties') `
        -l (Join-Path $loadtest "results\$name\results.jtl") -e -o (Join-Path $loadtest "results\$name\report") `
        @schedule -Jhost=localhost -Jport=8082
}
if ($LASTEXITCODE -ne 0) { throw 'JMeter gagal.' }
Write-Host "[OK] Report: $loadtest\results\$name\report\index.html" -ForegroundColor Green
