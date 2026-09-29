# Crea la llave de subida (upload key) de Perfila para Google Play.
# Ejecutar UNA sola vez en Windows:  clic derecho > "Ejecutar con PowerShell"
# o en PowerShell:  powershell -ExecutionPolicy Bypass -File scripts\crear-llave-firma.ps1
#
# La llave se guarda FUERA del repo, en %USERPROFILE%\perfila-llaves\
# Respáldala (USB + nube cifrada). Si la pierdes, no podrás firmar actualizaciones
# sin pedirle a Google un cambio de llave.

$ErrorActionPreference = "Stop"

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
if (-not $keytool) { throw "No encontré keytool. Instala Android Studio o un JDK 17 y vuelve a correr el script." }

$dir = Join-Path $env:USERPROFILE "perfila-llaves"
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$jks = Join-Path $dir "perfila-upload.jks"
if (Test-Path $jks) { throw "Ya existe $jks. No la sobrescribas: es tu llave de Google Play." }

Write-Host ""
Write-Host "keytool te pedirá una contraseña (mínimo 6 caracteres) y tus datos (nombre, empresa, ciudad, país MX)." -ForegroundColor Cyan
Write-Host "Guarda la contraseña en tu gestor de contraseñas." -ForegroundColor Cyan
Write-Host ""

& $keytool -genkeypair -v -storetype PKCS12 -keystore $jks -alias perfila -keyalg RSA -keysize 4096 -validity 10000
if ($LASTEXITCODE -ne 0) { throw "keytool terminó con error." }

$b64Path = Join-Path $dir "perfila-upload.jks.b64.txt"
[IO.File]::WriteAllText($b64Path, [Convert]::ToBase64String([IO.File]::ReadAllBytes($jks)))

Write-Host ""
Write-Host "Listo. Llave creada en: $jks" -ForegroundColor Green
Write-Host ""
Write-Host "Ahora agrega 4 secretos en GitHub:" -ForegroundColor Yellow
Write-Host "  github.com/andresguru/Perfila > Settings > Secrets and variables > Actions > New repository secret"
Write-Host ""
Write-Host "  PERFILA_KEYSTORE_BASE64   = todo el contenido de $b64Path"
Write-Host "  PERFILA_KEYSTORE_PASSWORD = la contraseña que acabas de escribir"
Write-Host "  PERFILA_KEY_ALIAS         = perfila"
Write-Host "  PERFILA_KEY_PASSWORD      = la misma contraseña"
Write-Host ""
Write-Host "Después de pegarlo en GitHub, BORRA el archivo .b64.txt (la .jks sí guárdala y respáldala)." -ForegroundColor Yellow
