# InkFold

InkFold is a local-first Android EPUB reader built with Jetpack Compose and the Readium Kotlin toolkit.

Current capabilities:

- editorial-style home shelf UI in Compose with an adaptive lazy grid and recent-imports rail
- system dark theme support across the Compose shelf and reader chrome
- in-app EPUB import through the system picker
- Android Open with support for local `.epub` files
- app-managed EPUB storage with SHA-256 deduplication
- fragment-hosted Readium EPUB reader
- hideable reader chrome over the reading surface
- live reader settings for theme, font size, and page margins
- DataStore-backed persistence for shared EPUB appearance defaults
- visible per-book actions for removal from the shelf
- persisted reading progress and resume

Project guidance for future implementation lives in `AGENTS.md`.
The current status and remaining page-transition work lives in `IMPROV.md`.

## Build Status

Verified on `2026-03-28`:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebugAndroidTest`
- `./gradlew :app:lintDebug`

No emulator/device was available for connected instrumentation tests or manual file-manager acceptance at verification time.

## Notes

- The app is EPUB-only in the current scope.
- Reader settings currently edit shared EPUB defaults, not per-book overrides.
- The page-flip work in this branch is scaffolding only; no interactive 3D flip is enabled yet.
- The current branch keeps `android.newDsl=false` and `android.builtInKotlin=false` in `gradle.properties` as a compatibility workaround for the AGP/KSP setup in this repo.
- The bundled `readium-kotlin-toolkit-3.1.2/test-app` directory is the primary implementation reference for Readium integration details.
