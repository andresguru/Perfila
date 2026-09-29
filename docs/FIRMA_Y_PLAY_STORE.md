# Firma de la app y publicación en Google Play

## Cómo está armado

| Build | Firma | Para qué |
|---|---|---|
| `debug` (`mx.perfila.app.debug`) | Llave de depuración automática | Pruebas internas. Convive con la versión oficial en el teléfono. |
| `release` (`mx.perfila.app`) | **Llave de subida de Perfila** | APK firmado para instalar y AAB para Google Play. |

- `versionCode` = 100 + número de build de GitHub Actions. Sube solo en cada push, así Play siempre acepta la actualización.
- `versionName` = `0.1.<build>`. Para cambiar a 0.2, edita `appVersionName` en `app/build.gradle.kts`.
- La llave nunca vive en el repo. En CI se reconstruye desde un secreto y se borra al terminar el build.

## Paso 1. Crear la llave (una sola vez)

En Windows, desde la carpeta del repo:

```
powershell -ExecutionPolicy Bypass -File scripts\crear-llave-firma.ps1
```

Queda en `%USERPROFILE%\perfila-llaves\perfila-upload.jks`. **Respáldala** en dos lugares.

## Paso 2. Agregar los secretos en GitHub

`Settings > Secrets and variables > Actions > New repository secret`

| Secreto | Valor |
|---|---|
| `PERFILA_KEYSTORE_BASE64` | Contenido de `perfila-upload.jks.b64.txt` |
| `PERFILA_KEYSTORE_PASSWORD` | Tu contraseña |
| `PERFILA_KEY_ALIAS` | `perfila` |
| `PERFILA_KEY_PASSWORD` | La misma contraseña |

Después borra el `.b64.txt`.

## Paso 3. Builds firmados

Cada push a `main` genera en Actions:

- `perfila-release-apk`: APK firmado para instalar en el teléfono.
- `perfila-release-aab`: paquete para subir a Google Play.

Para publicar una versión con link fijo de descarga:

```
git tag v0.1.0
git push origin v0.1.0
```

Eso crea una página en **Releases** con el APK y el AAB adjuntos.

## Paso 4. Google Play

1. Crea la cuenta de desarrollador en Play Console (pago único).
2. Crea la app **Perfila** con el paquete `mx.perfila.app`.
3. Acepta **Play App Signing**: Google guarda la llave final y tu `.jks` queda como llave de subida. Si algún día pierdes la `.jks`, Google puede reemplazarla.
4. Sube `app-release.aab` a **Pruebas internas** primero, luego cerradas, luego producción.
5. Pendientes antes de producción: aviso de privacidad publicado (URL), ficha de la tienda, capturas, clasificación de contenido y formulario de seguridad de datos.

## Nota al instalar en tu teléfono

El APK de debug y el firmado tienen paquetes distintos, así que puedes tener ambos. Las actualizaciones del APK firmado se instalan encima sin perder datos siempre que vengan firmadas con la misma llave.
