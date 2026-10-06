# Astha TV — Android TV base project

Base compilable de una interfaz de streaming para Android TV / Google TV / Fire TV.

## Incluido

- Kotlin + Jetpack Compose
- Interfaz 10-foot en landscape
- Navegación por D-Pad/foco
- `LazyColumn` + `LazyRow` con claves estables
- Animación premium de foco sin bordes
- Carga de imágenes con Coil 3
- Pantalla Home + Detalles
- Reproductor Media3 / ExoPlayer
- Cliente asíncrono de Stremio Addons v3 (`manifest`, `catalog`, `meta`, `stream`, `subtitles`)
- Contrato `TorrentEngine` preparado para implementar libtorrent/JNI
- Launcher para TV (`LEANBACK_LAUNCHER`)

## Requisitos

- Android Studio compatible con AGP 9.4
- JDK 17
- Android SDK 37
- Gradle 9.6 (bootstrap `gradlew` incluido; descarga la distribución fijada en el primer build)

## Compilar

1. Descomprime el ZIP.
2. Android Studio → **Open** → selecciona la carpeta `AsthaTV`.
3. Espera el Gradle Sync.
4. Instala SDK 37 si Android Studio lo solicita.
5. Build → Build APK(s).

O por terminal:

```bash
./gradlew assembleDebug
```

APK esperado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Probar

Puedes usar un emulador Android TV/Google TV en Android Studio o un dispositivo Android TV con depuración ADB.

La pantalla de detalles incluye un vídeo público de prueba para comprobar Media3.

## Stremio Addons

Cliente:

```text
app/src/main/java/com/asthatv/app/data/StremioAddonClient.kt
```

Ejemplo:

```kotlin
val addon = StremioAddonClient("https://TU-ADDON/manifest.json")
val manifest = addon.manifest()
val streams = addon.streams("movie", "tt1375666")
```

No registres en logs URLs privadas de addons: pueden contener tokens.

## Torrent/P2P

El ZIP contiene la arquitectura/contrato, **no una librería binaria de libtorrent**. El punto de integración es:

```text
app/src/main/java/com/asthatv/app/player/PlaybackSource.kt
```

La implementación final debe ser un módulo C++/JNI que gestione metadata, DHT, peers, selección de archivo y prioridades/deadlines de piezas, y exponga al reproductor una fuente local/seekable. Mantener esta capa separada evita bloquear la UI.

Usa P2P únicamente para contenido que tengas derecho a reproducir.

## Nota sobre orientación

La Activity solicita `landscape`. En Android TV es el comportamiento esperado. Android moderno puede ignorar restricciones de orientación en determinadas pantallas grandes, por lo que la UI también está diseñada como landscape-first.

## BitTorrent engine (added)

This build includes **libtorrent 2.x through FrostWire jlibtorrent/JNI** for Android TV.
Supported packaged ABIs: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`.

Main files:
- `torrent/TorrentEngine.kt` — backend contract.
- `torrent/LibtorrentEngine.kt` — native libtorrent implementation.
- `torrent/GrowingFileHttpServer.kt` — loopback HTTP Range bridge for Media3.
- `torrent/TorrentModels.kt` — selectable video files and state.

Typical flow:
1. Receive a legal magnet URI from an addon/user.
2. `inspectMagnet()` obtains metadata over DHT/trackers and lists video files.
3. `streamMagnet()` selects the requested/largest video and gives it maximum file priority.
4. libtorrent downloads in sequential mode into the app cache.
5. Media3 receives the returned `http://127.0.0.1:PORT/stream` URL.

The engine is intended for content the user is authorized to download/stream. It does not bypass DRM.

## Flujo Stremio -> BitTorrent -> Media3

Esta revisión incorpora `StremioStreamResolver`, `StremioPlaybackRepository` y `PlaybackCoordinator`.
Un stream Stremio con `infoHash` se convierte automáticamente en magnet; un `url` magnet se envía al motor P2P; HTTP/HTTPS va directo a Media3. `fileIdx` se conserva para seleccionar el archivo correcto dentro de torrents multiarchivo.

El reproductor muestra un HUD P2P discreto con progreso, descarga/subida, peers y seeds cuando el backend publique esas métricas. El servidor HTTP local soporta peticiones Range para que Media3 pueda leer un archivo en crecimiento.

Para conectar una pantalla real de fuentes:

```kotlin
val streams = StremioPlaybackRepository().sources(addon, "movie", imdbId)
val source = streams.first().second
// Navegar a Screen.Player(source)
```

Nota: usa BitTorrent únicamente con contenido que tengas derecho a reproducir/distribuir. La aplicación no evade DRM.

## Premium category navigation
Home now includes a borderless 10-foot category rail designed for D-Pad: opacity hierarchy, restrained focus scaling, a minimal active indicator, stable LazyRow keys/content types, and category-driven catalog filtering without replacing the TV lazy-loading architecture.

## Premium retractable TV sidebar
This build adds a borderless 10-foot navigation rail for D-Pad use. It stays compact over content and expands only while the rail owns focus. Destinations: Home, Search, Library, Addons, Downloads and Settings. Placeholder destinations are intentionally lightweight so real features can be wired without changing the shell navigation.


## Release optimization (R8 / ProGuard)

The release variant uses the AGP 9.4 `optimization { enable = true }` pipeline. This enables R8 code shrinking/optimization/obfuscation and optimized resource shrinking. Debug remains unminified.

The jlibtorrent JNI bridge is protected by the narrow rule in `app/src/main/keepRules/astha-release.keep`; the rest of the SWIG package is intentionally not blanket-kept so R8 can remove unused code.

Build the optimized artifact with:

```bash
./gradlew assembleRelease
```

For Play distribution:

```bash
./gradlew bundleRelease
```

Keep `mapping.txt` from `app/build/outputs/mapping/release/` for crash retracing. Test torrent startup, playback, subtitles and addon parsing on a release build before publishing.

## Optimización TV (revisión correcta)

- R8 + resource shrinking se aplican únicamente a `release` mediante AGP `optimization { enable = true }`.
- Las reglas de jlibtorrent conservan el bridge JNI requerido sin desactivar la optimización del resto de la app.
- Los posters se decodifican cerca de su tamaño real en pantalla y sin crossfade durante scroll/foco para reducir RAM, decodificación y GPU overdraw.
- El foco usa una sola `graphicsLayer` para escala, sombra y clipping, evitando encadenar `shadow()` + `clip()` animados.
- Hay APKs por ABI (`arm64-v8a`, `armeabi-v7a`, x86, x86_64) y también universal. Para una TV real instala el APK de su ABI: evita transportar las otras bibliotecas nativas de libtorrent.
- Gradle caching, parallelism, configuration cache e incremental Kotlin aceleran compilaciones; no se confunden con optimizaciones runtime.
- No se añadieron `-dontwarn`, `-dontoptimize` ni reglas `keep` globales: esas reglas suelen ocultar problemas o anular R8.

Para medir rendimiento usa siempre `release`; Debug no representa el coste real de Compose/R8.


## Baseline Profiles + Macrobenchmark

This build includes two performance-only modules:

- `:baselineprofile`: generates an ART Baseline Profile for cold launch and core TV D-Pad navigation.
- `:benchmark`: measures cold startup and frame timing during deterministic D-Pad traversal.

Generate the profile on a connected Android TV/emulator:

```bash
./gradlew :app:generateBaselineProfile
```

Run macrobenchmarks:

```bash
./gradlew :benchmark:connectedCheck
```

Use a release-like build/device for meaningful numbers. The benchmark path intentionally avoids network and BitTorrent dependencies so external latency does not contaminate UI measurements.


## AppGallery integrity / backend security

Added `security/integrity/` with a one-time challenge → provider proof → server authorization flow and a fail-closed backend reference under `security-backend/`.

Huawei-specific credentials are intentionally not included. Complete `APPGALLERY_INTEGRITY_SETUP.md` after creating the real AppGallery Connect application and release signing certificate.
