<#
.SYNOPSIS
  Mengambil access token dari Keycloak realm tokoku (compose profil security).
.DESCRIPTION
  -User alice|bob : authorization code + PKCE (client tokoku-web) tanpa browser. Skrip mengisi form login
                    Keycloak seperti browser; TIDAK memakai password grant (slide 92: tanpa password grant).
  -ClientCredentials : token service-to-service client tokoku-loadtest (scope payments.write).
  -OtherApi         : token untuk API lain (aud inventory-service), untuk D11.3.
  Token dicetak ke stdout supaya bisa ditampung: $token = .\scripts\get-token.ps1 -User alice
.EXAMPLE
  $alice = .\scripts\get-token.ps1 -User alice
  curl.exe -H "Authorization: Bearer $alice" localhost:8082/api/orders
.EXAMPLE
  .\scripts\get-token.ps1 -ClientCredentials -Decode
#>
param(
    [ValidateSet('alice', 'bob')] [string]$User,
    [switch]$ClientCredentials,
    [switch]$OtherApi,
    [switch]$Decode,
    [switch]$Copy,
    [string]$Issuer = 'http://localhost:8180/realms/tokoku'
)

$ErrorActionPreference = 'Stop'
$tokenEndpoint = "$Issuer/protocol/openid-connect/token"

function ConvertTo-Base64Url([byte[]]$bytes) {
    [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

function Get-PkceToken([string]$username) {
    $verifier = ConvertTo-Base64Url ((1..48 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
    $sha = [System.Security.Cryptography.SHA256]::Create()
    $challenge = ConvertTo-Base64Url ($sha.ComputeHash([Text.Encoding]::ASCII.GetBytes($verifier)))
    $redirect = 'http://localhost/callback'
    $authUrl = "$Issuer/protocol/openid-connect/auth?client_id=tokoku-web&response_type=code&scope=openid" +
        "&redirect_uri=$([uri]::EscapeDataString($redirect))&code_challenge=$challenge&code_challenge_method=S256&state=lab"

    # 1. Halaman login. Cookie session Keycloak bertanda Secure: browser tetap mengirimnya ke http://localhost,
    #    tetapi .NET tidak, jadi cookie diteruskan manual lewat header Cookie.
    $login = Invoke-WebRequest -UseBasicParsing -Uri $authUrl
    $cookies = ([regex]::Matches([string]$login.Headers['Set-Cookie'], '(AUTH_SESSION_ID|KC_AUTH_SESSION_HASH|KC_RESTART)=([^;,]+)') |
        ForEach-Object { "$($_.Groups[1].Value)=$($_.Groups[2].Value)" }) -join '; '
    if ($login.Content -notmatch 'action="([^"]+)"') { throw 'Form login Keycloak tidak ditemukan.' }
    $action = [System.Net.WebUtility]::HtmlDecode($Matches[1])

    # 2. Submit username/password seperti browser; Keycloak membalas 302 ke redirect_uri dengan ?code=...
    #    HttpClient dipakai karena Invoke-WebRequest (PowerShell 5.1) membuang header Cookie manual.
    Add-Type -AssemblyName System.Net.Http
    $handler = New-Object System.Net.Http.HttpClientHandler
    $handler.UseCookies = $false
    $handler.AllowAutoRedirect = $false
    $client = New-Object System.Net.Http.HttpClient($handler)
    try {
        $form = New-Object 'System.Collections.Generic.Dictionary[string,string]'
        $form.Add('username', $username); $form.Add('password', $username); $form.Add('credentialId', '')
        $request = New-Object System.Net.Http.HttpRequestMessage([System.Net.Http.HttpMethod]::Post, $action)
        $request.Content = New-Object System.Net.Http.FormUrlEncodedContent($form)
        $request.Headers.Add('Cookie', $cookies)
        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        $location = if ($response.Headers.Location) { $response.Headers.Location.ToString() } else { $null }
    } finally {
        $client.Dispose()
    }
    if (-not $location -or $location -notmatch '[?&]code=([^&]+)') { throw "Login $username gagal (tidak ada authorization code)." }
    $code = $Matches[1]

    # 3. Tukar code + code_verifier menjadi token.
    $token = Invoke-RestMethod -Method Post -Uri $tokenEndpoint -Body @{
        grant_type = 'authorization_code'; client_id = 'tokoku-web'; code = $code
        redirect_uri = $redirect; code_verifier = $verifier
    }
    return $token.access_token
}

function Get-ClientToken([string]$clientId, [string]$secret) {
    (Invoke-RestMethod -Method Post -Uri $tokenEndpoint -Body @{
        grant_type = 'client_credentials'; client_id = $clientId; client_secret = $secret
    }).access_token
}

if ($User) { $accessToken = Get-PkceToken $User }
elseif ($ClientCredentials) { $accessToken = Get-ClientToken 'tokoku-loadtest' 'tokoku-loadtest-secret' }
elseif ($OtherApi) { $accessToken = Get-ClientToken 'tokoku-other-api' 'tokoku-other-api-secret' }
else { throw 'Pilih -User alice|bob, -ClientCredentials, atau -OtherApi.' }

if ($Decode) {
    $payload = $accessToken.Split('.')[1].Replace('-', '+').Replace('_', '/')
    $payload += '=' * ((4 - $payload.Length % 4) % 4)
    $claims = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload)) | ConvertFrom-Json
    $claims | Select-Object iss, aud, sub, azp, scope, preferred_username,
        @{ n = 'exp'; e = { [DateTimeOffset]::FromUnixTimeSeconds($_.exp).LocalDateTime } } | Format-List | Out-Host
}
if ($Copy) { Set-Clipboard -Value $accessToken; Write-Host '[OK] Token disalin ke clipboard.' -ForegroundColor Green }
$accessToken
