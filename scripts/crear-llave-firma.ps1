# Crea la llave de subida (upload key) de Perfila y, si tienes GitHub CLI,
# carga los 4 secretos en el repo automáticamente.
#
# Ejecutar UNA sola vez en Windows, desde la carpeta del repo:
#   powershell -ExecutionPolicy Bypass -File scripts\crear-llave-firma.ps1
#
# Solo te pide una contraseña. La llave se guarda FUERA del repo, en
# %USERPROFILE%\perfila-llaves\. Respáldala: todas las actualizaciones se firman con ella.

$ErrorActionPreference = "Stop"
$Repo  = "andresguru/Perfila"
$Alias = "perfila"
$DName = "CN=Perfila, OU=Movil, O=Perfila, L=Chihuahua, ST=Chihuahua, C=MX"

# --- Buscar keytool (viene con Android Studio) ---
$candidates = @(
    $(if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\keytool.exe" }),
    "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe",
    "C:\Program Files\Android\Android Studio\jre\bin\keytool.exe"
)
$keytool = $candidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (-not $keytool) {
    $cmd = Get-Command keytool -ErrorAction SilentlyContinue
    if ($cmd) { $keytool = $cmd.Source }
}
if (-not $keytool) { throw "No encontré keytool. Instala Android Studio (o un JDK 17) y vuelve a correr el script." }

$dir = Join-Path $env:USERPROFILE "perfila-llaves"
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$jks = Join-Path $dir "perfila-upload.jks"
$existing = Test-Path $jks
if ($existing) {
    Write-Host "Ya existe $jks. No se crea otra: solo se cargarán los secretos." -ForegroundColor Yellow
}

# --- Pedir contraseña una sola vez ---
function Read-Plain([string]$prompt) {
    $sec = Read-Host -AsSecureString $prompt
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
if ($existing) {
    $pw1 = Read-Plain "Contraseña de tu llave existente"
} else {
    do {
        $pw1 = Read-Plain "Elige una contraseña para la llave (mínimo 8 caracteres)"
        $pw2 = Read-Plain "Repítela"
        $ok = ($pw1 -eq $pw2) -and ($pw1.Length -ge 8)
        if (-not $ok) { Write-Host "No coinciden o es muy corta. Intenta de nuevo." -ForegroundColor Red }
    } until ($ok)
}

# --- Crear la llave (la contraseña viaja por variable de entorno, no por línea de comandos) ---
if (-not $existing) {
$env:PERFILA_KS_PW = $pw1
try {
    & $keytool -genkeypair -noprompt -storetype PKCS12 -keystore $jks -alias $Alias `
        -keyalg RSA -keysize 4096 -validity 10000 -dname $DName `
        -storepass:env PERFILA_KS_PW -keypass:env PERFILA_KS_PW
    if ($LASTEXITCODE -ne 0) { throw "keytool terminó con error." }
} finally {
    Remove-Item Env:\PERFILA_KS_PW -ErrorAction SilentlyContinue
}
Write-Host "Llave creada: $jks" -ForegroundColor Green
} else {
    $env:PERFILA_KS_PW = $pw1
    try {
        & $keytool -list -storetype PKCS12 -keystore $jks -storepass:env PERFILA_KS_PW | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "La contraseña no abre $jks. Vuelve a intentarlo." }
    } finally {
        Remove-Item Env:\PERFILA_KS_PW -ErrorAction SilentlyContinue
    }
}

$b64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($jks))

# --- Cargar secretos con GitHub CLI si está disponible ---
$gh = Get-Command gh -ErrorAction SilentlyContinue
$uploaded = $false
if ($gh) {
    $ErrorActionPreference = "Continue"   # gh escribe avisos en stderr; no son errores
    & gh auth status *> $null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Inicia sesión en GitHub CLI (se abrirá el navegador)..." -ForegroundColor Cyan
        & gh auth login --web --git-protocol https
    }
    if ($LASTEXITCODE -eq 0) {
        $uploaded = $true
        foreach ($pair in @(
            @("PERFILA_KEYSTORE_BASE64", $b64),
            @("PERFILA_KEYSTORE_PASSWORD", $pw1),
            @("PERFILA_KEY_ALIAS", $Alias),
            @("PERFILA_KEY_PASSWORD", $pw1))) {
            & gh secret set $pair[0] --repo $Repo --body $pair[1]
            if ($LASTEXITCODE -ne 0) { $uploaded = $false }
        }
    }
    $ErrorActionPreference = "Stop"
}

if ($uploaded) {
    Write-Host ""
    Write-Host "Listo: los 4 secretos quedaron cargados en $Repo." -ForegroundColor Green
    Write-Host "El siguiente push a main generará el APK firmado y el AAB para Google Play."
} else {
    $b64Path = Join-Path $dir "perfila-upload.jks.b64.txt"
    [IO.File]::WriteAllText($b64Path, $b64)
    Write-Host ""
    Write-Host "No pude cargar los secretos con GitHub CLI (gh). Instálalo con:  winget install GitHub.cli" -ForegroundColor Yellow
    Write-Host "y vuelve a correr este mismo script (no crea otra llave), o agrégalos a mano en:"
    Write-Host "  https://github.com/$Repo/settings/secrets/actions"
    Write-Host "  PERFILA_KEYSTORE_BASE64   = contenido de $b64Path"
    Write-Host "  PERFILA_KEYSTORE_PASSWORD = tu contraseña"
    Write-Host "  PERFILA_KEY_ALIAS         = $Alias"
    Write-Host "  PERFILA_KEY_PASSWORD      = tu contraseña"
    Write-Host "Después borra el archivo .b64.txt." -ForegroundColor Yellow
}

$pw1 = $null; $pw2 = $null
Write-Host ""
Write-Host "IMPORTANTE: respalda $jks y tu contraseña (gestor de contraseñas + copia en USB)." -ForegroundColor Cyan
