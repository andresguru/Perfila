# Perfila

**El perfil correcto para el puesto correcto.**

Perfila es una app de reclutamiento por match para México y LATAM. Candidatos y reclutadores deslizan tarjetas y, cuando ambos dicen que sí, se abre el chat. Android primero.

## Qué hace diferente a Perfila

- **Salario obligatorio** en cada vacante. Si los rangos no se cruzan, no se muestran.
- **Match explicable:** cada tarjeta muestra el % de compatibilidad y por qué.
- **Garantía de respuesta:** la empresa tiene 72 h para escribir después del match.
- **Tiempo de traslado real** para vacantes presenciales.
- **Modo ciego** que oculta nombre, foto y edad hasta el match.

## Gestos

| Gesto | Acción |
|---|---|
| Deslizar a la derecha | Me interesa |
| Deslizar a la izquierda | Descartar |
| Deslizar hacia arriba | Destacar (3 al día) |

## Estado actual (v0.1.0)

MVP navegable con datos de muestra y sin backend:

- Bienvenida, registro con selección de rol (candidato o reclutador)
- Onboarding del candidato: experiencia, habilidades, salario, modalidad, traslado
- Publicar vacante con rango salarial obligatorio
- Mazo deslizable de vacantes y de perfiles, con botones y gestos
- Algoritmo de match v1 con pruebas unitarias (`MatchScorer`)
- Pantalla de match, lista de matches con contador de 72 h y chat

## Stack

- Kotlin + Jetpack Compose (Material 3), Navigation Compose
- minSdk 26, targetSdk 36
- Backend planeado: Supabase (Postgres + PostGIS + Auth + Realtime). Esquema inicial en `supabase/migrations/0001_init.sql`.
- CI: GitHub Actions compila, corre pruebas y publica el APK de debug como artefacto.

## Estructura

```
app/src/main/java/mx/perfila/app/
  domain/        Modelos y algoritmo de match (MatchScorer)
  data/          Datos de muestra (se reemplazará por repositorio Supabase)
  ui/components/ SwipeDeck, tarjetas, isotipo y componentes comunes
  ui/screens/    Bienvenida, registro, formularios, principal, chat
  ui/theme/      Colores y tipografía de marca
supabase/        Migraciones SQL
docs/            Plan ejecutivo v1 (redactado con el nombre de trabajo "Jale")
```

## Correr el proyecto

1. Abre la carpeta en Android Studio (versión reciente con soporte para AGP 8.11).
2. Espera la sincronización de Gradle.
3. Ejecuta la configuración `app` en un emulador o teléfono con Android 8 o superior.

Pruebas unitarias:

```
./gradlew testDebugUnitTest
```

## Algoritmo de match v1

Primero se aplican filtros duros (salario que se cruza, modalidad aceptada, traslado dentro del límite). Luego:

| Factor | Peso |
|---|---|
| Habilidades (imprescindibles y deseables) | 30% |
| Salario | 25% |
| Ubicación y modalidad | 20% |
| Experiencia | 15% |
| Disponibilidad | 10% |

## Siguientes pasos

1. Integrar Supabase: autenticación por teléfono (WhatsApp) y Google, perfiles y vacantes reales.
2. Swipes y matches en el servidor (trigger de doble sí ya definido en la migración).
3. Chat en tiempo real y notificaciones push (FCM).
4. Fuentes de marca (Bricolage Grotesque y Figtree), modo oscuro, textos en `strings.xml`.
5. Verificación de empresas (RFC) y candidatos, video de presentación de 30 s.

## Marca

| Color | Hex | Uso |
|---|---|---|
| Violeta | `#5B3DF5` | Marca, acciones principales |
| Lima | `#C5F04A` | Me interesa, match |
| Mandarina | `#FF6A3D` | Descartar |
| Tinta | `#16132E` | Texto |
| Hueso | `#F6F4EE` | Fondo |
