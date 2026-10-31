<#
.SYNOPSIS
  Cek kesiapan laptop untuk kelas TokoKu (rencana lab bagian 4.1). Hasil wajib tanpa [FAIL].
.DESCRIPTION
  [OK]   siap
  [WARN] opsional / perlu dicek manual, tidak menghalangi kelas
  [FAIL] harus diperbaiki sebelum kelas
  Kirim screenshot hasilnya ke instruktur pada H-2.
.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\scripts\verify-env.ps1
#>
param(
    # Lewati cek port, mis. saat infra kelas sudah berjalan.
    [switch]$SkipPorts
)

$ErrorActionPreference = 'Continue'
$script:fails = 0
$script:warns = 0

function Report([string]$status, [string]$item, [string]$detail) {
    switch ($status) {
        'OK'   { $color = 'Green' }
        'WARN' { $color = 'Yellow'; $script:warns++ }
        'FAIL' { $color = 'Red';    $script:fails++ }
    }
    Write-Host ('[{0,-4}] ' -f $status) -ForegroundColor $color -NoNewline
    Write-Host ('{0,-34} {1}' -f $item, $detail)
}

function Has-Command([string]$name) {
    return [bool](Get-Command $name -ErrorAction SilentlyContinue)
}

function Java-Major([string]$javaExe) {
    $out = & $javaExe -version 2>&1 | Out-String
    if ($out -match 'version "(\d+)') { return [int]$Matches[1] }
    return 0
}

Write-Host ''
Write-Host '=== TokoKu: cek lingkungan ===' -ForegroundColor Cyan

# --- Perangkat keras ---------------------------------------------------------
$ramGb = [math]::Round((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB, 1)
if ($ramGb -ge 30) { Report OK 'RAM' "$ramGb GB" } else { Report FAIL 'RAM' "$ramGb GB (butuh 32 GB)" }

$cores = (Get-CimInstance Win32_Processor | Measure-Object -Property NumberOfCores -Sum).Sum
if ($cores -ge 4) { Report OK 'CPU core' "$cores" } else { Report FAIL 'CPU core' "$cores (butuh 4+)" }

$drive = (Get-Item $PSScriptRoot).PSDrive.Name
$freeGb = [math]::Round((Get-PSDrive $drive).Free / 1GB, 0)
if ($freeGb -ge 30) { Report OK "Disk kosong ${drive}:" "$freeGb GB" } else { Report FAIL "Disk kosong ${drive}:" "$freeGb GB (butuh 30 GB)" }

# --- JDK ---------------------------------------------------------------------
if (Has-Command 'java') {
    $major = Java-Major 'java'
    if ($major -eq 21) {
        Report OK 'java -version (default)' '21'
    } else {
        Report FAIL 'java -version (default)' "$major. Default harus 21. Installer JDK 25 sering mengambil alih PATH/JAVA_HOME; kembalikan ke 21."
    }
} else {
    Report FAIL 'java' 'tidak ada di PATH'
}

if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $major = Java-Major "$env:JAVA_HOME\bin\java.exe"
    if ($major -eq 21) { Report OK 'JAVA_HOME' $env:JAVA_HOME } else { Report FAIL 'JAVA_HOME' "$env:JAVA_HOME (versi $major, harus 21)" }
} else {
    Report FAIL 'JAVA_HOME' 'belum diset atau tidak valid'
}

$jdk25 = $null
if ($env:JDK25_HOME -and (Test-Path "$env:JDK25_HOME\bin\java.exe")) {
    $jdk25 = $env:JDK25_HOME
} else {
    $jdk25 = Get-ChildItem 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java' -Directory -Filter 'jdk-25*' -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if ($jdk25 -and (Java-Major "$jdk25\bin\java.exe") -eq 25) {
    Report OK 'JDK 25' $jdk25
    if (-not $env:JDK25_HOME) { Report WARN 'JDK25_HOME' "belum diset; disarankan: setx JDK25_HOME `"$jdk25`"" }
} else {
    Report FAIL 'JDK 25' 'tidak ditemukan (pasang Temurin 25, set JDK25_HOME)'
}

foreach ($tool in 'jcmd', 'jfr') {
    if (Has-Command $tool) { Report OK $tool (Get-Command $tool).Source } else { Report FAIL $tool 'tidak ada di PATH (bagian dari JDK 21)' }
}

# --- Git, Docker, Kubernetes -------------------------------------------------
if (Has-Command 'git') { Report OK 'git' ((git --version) -replace 'git version ', '') } else { Report FAIL 'git' 'tidak terpasang' }

$dockerOk = $false
if (Has-Command 'docker') {
    $serverVersion = docker version --format '{{.Server.Version}}' 2>$null
    if ($LASTEXITCODE -eq 0 -and $serverVersion) {
        $dockerOk = $true
        Report OK 'Docker Engine' $serverVersion
        $memGb = [math]::Round([double](docker info --format '{{.MemTotal}}' 2>$null) / 1GB, 1)
        if ($memGb -ge 12) { Report OK 'Memori Docker' "$memGb GB" } else { Report FAIL 'Memori Docker' "$memGb GB (atur 12-16 GB lewat .wslconfig)" }
        $composeVersion = docker compose version --short 2>$null
        if ($LASTEXITCODE -eq 0) { Report OK 'docker compose' $composeVersion } else { Report FAIL 'docker compose' 'plugin compose v2 tidak ada' }
    } else {
        Report FAIL 'Docker Engine' 'Docker Desktop belum berjalan'
    }
} else {
    Report FAIL 'docker' 'Docker Desktop belum terpasang'
}

if (Has-Command 'kubectl') {
    $contexts = kubectl config get-contexts -o name 2>$null
    if ($contexts -contains 'docker-desktop') {
        Report OK 'kubectl context' 'docker-desktop'
        $nodes = kubectl --context docker-desktop get nodes --no-headers 2>$null
        if ($LASTEXITCODE -ne 0 -or -not $nodes) {
            Report FAIL 'Kubernetes Docker Desktop' 'tidak menjawab (aktifkan di Settings > Kubernetes)'
        } else {
            $nodeLines = @($nodes)
            if ($nodeLines.Count -eq 1 -and $nodeLines[0] -match '^docker-desktop\s+Ready') {
                Report OK 'Kubernetes Docker Desktop' 'node Ready, mode kubeadm'
            } elseif ($nodeLines[0] -match 'control-plane|worker') {
                Report FAIL 'Kubernetes Docker Desktop' 'mode kind terdeteksi; pilih kubeadm (image lokal tidak terlihat di mode kind)'
            } else {
                Report FAIL 'Kubernetes Docker Desktop' "node belum Ready: $($nodeLines[0])"
            }
        }
    } else {
        Report FAIL 'kubectl context' 'context docker-desktop tidak ada (aktifkan Kubernetes di Docker Desktop)'
    }
} else {
    Report FAIL 'kubectl' 'tidak ada di PATH'
}

# --- Alat bantu --------------------------------------------------------------
if (Has-Command 'curl.exe') { Report OK 'curl' 'ada' } else { Report FAIL 'curl' 'tidak ada' }
if (Has-Command 'jq') { Report OK 'jq' (jq --version) } else { Report FAIL 'jq' 'tidak ada (winget install jqlang.jq)' }

$jmeter = $null
if (Has-Command 'jmeter') { $jmeter = (Get-Command jmeter).Source }
elseif ($env:JMETER_HOME -and (Test-Path "$env:JMETER_HOME\bin\jmeter.bat")) { $jmeter = "$env:JMETER_HOME\bin\jmeter.bat" }
if ($jmeter) { Report OK 'JMeter' $jmeter } else { Report FAIL 'JMeter 5.6.x' 'tidak ditemukan (PATH atau JMETER_HOME)' }

$ides = @()
if (Has-Command 'code') { $ides += 'VS Code' }
if ((Has-Command 'idea64') -or (Has-Command 'idea') -or (Get-ChildItem "$env:ProgramFiles\JetBrains" -Directory -Filter 'IntelliJ*' -ErrorAction SilentlyContinue)) { $ides += 'IntelliJ IDEA' }
if ($ides.Count -gt 0) { Report OK 'IDE' ($ides -join ', ') } else { Report WARN 'IDE' 'VS Code / IntelliJ tidak terdeteksi otomatis; pastikan salah satu terpasang' }

Report WARN 'Bruno/Postman, JMC, MAT/VisualVM' 'cek manual: aplikasi GUI tidak dideteksi otomatis'

# --- Port --------------------------------------------------------------------
if (-not $SkipPorts) {
    $ports = 5432, 8081, 8082, 8083, 8084, 9081, 9082, 9083, 9084, 8180, 8761, 9092, 3000, 4317, 4318, 6379, 8474, 18083
    $busy = @()
    foreach ($port in $ports) {
        $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($conn) {
            $owner = (Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue).ProcessName
            # Port yang dipegang Docker Desktop berarti container TokoKu sudah jalan: bukan masalah.
            if ($owner -notmatch 'docker|wslrelay|vpnkit') { $busy += "$port ($owner)" }
        }
    }
    if ($busy.Count -eq 0) { Report OK 'Port lab' 'bebas' } else { Report FAIL 'Port lab' ("dipakai aplikasi lain: " + ($busy -join ', ')) }
}

# --- Image Docker (diisi lewat scripts/prepull.ps1) ---------------------------
$imageList = Join-Path $PSScriptRoot 'images.txt'
if ($dockerOk -and (Test-Path $imageList)) {
    $missing = @()
    foreach ($image in Get-Content $imageList | Where-Object { $_ -and -not $_.StartsWith('#') }) {
        docker image inspect $image *> $null
        if ($LASTEXITCODE -ne 0) { $missing += $image }
    }
    if ($missing.Count -eq 0) { Report OK 'Image Docker' 'semua sudah ditarik' } else { Report FAIL 'Image Docker' ("belum ditarik (jalankan prepull.ps1): " + ($missing -join ', ')) }
}

Write-Host ''
if ($script:fails -eq 0) {
    Write-Host "HIJAU: siap untuk kelas ($script:warns peringatan untuk dicek manual)." -ForegroundColor Green
    exit 0
} else {
    Write-Host "BELUM SIAP: $script:fails item [FAIL] harus diperbaiki." -ForegroundColor Red
    exit 1
}
