# InkFold

InkFold is a local-first Android EPUB reader built with Jetpack Compose and the Readium Kotlin toolkit.

Current capabilities:

- editorial-style home shelf UI in Compose with an adaptive lazy grid and recent-imports rail
- persisted app palette presets for shelf and reader chrome, now expanded to eight curated Material 3-derived options
- system light/dark theme support across the Compose shelf and reader chrome
- in-app EPUB import through the system picker
- Android Open with support for local `.epub` files
- app-managed EPUB storage with SHA-256 deduplication
- fragment-hosted Readium EPUB reader
- hideable reader chrome over the reading surface
- live reader settings for theme, paged-versus-scroll mode, text size, typeface, dark-theme image filters, and discrete page margins
- bottom reader page-navigation sheet for quick progression jumps and outline access
- Compose outline sheet for Contents, Pages, and Landmarks when the EPUB exposes them
- Readium-powered read aloud with spoken-text highlight, follow-along page sync, and speed/pitch/language/voice settings
- DataStore-backed persistence for shared EPUB appearance defaults
- separate DataStore-backed persistence for TTS preferences
- visible per-book actions for removal from the shelf
- persisted reading progress and resume

Project guidance for future implementation lives in `AGENTS.md`.
The current reader roadmap and pending implementation plan live in `IMPROV.md`.

## Build Status

Verified on `2026-03-29`:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebugAndroidTest`
- `./gradlew :app:lintDebug`

No emulator/device was available for connected instrumentation tests, in-reader manual acceptance, or external file-manager acceptance at verification time.

## Notes

- The app is EPUB-only in the current scope.
- App settings currently let you switch among eight built-in InkFold palette presets.
- Reader settings currently edit shared EPUB defaults, not per-book overrides.
- Reader outline access is exposed from the bottom progress chrome and hides empty sections automatically.
- Read aloud uses Android TTS through Readium and prompts for voice-data installation when a required language is missing.
- Reader quick-jump navigation is progression-based because EPUB page counts shift with theme, font size, and margins.
- The page-flip work in this branch is scaffolding only; no interactive 3D flip is enabled yet.
- The current branch keeps `android.newDsl=false` and `android.builtInKotlin=false` in `gradle.properties` as a compatibility workaround for the AGP/KSP setup in this repo.
- The bundled `readium-kotlin-toolkit-3.1.2/test-app` directory is the primary implementation reference for Readium integration details.
