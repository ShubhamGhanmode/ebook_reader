# InkFold Agent Guide

## Project Overview

InkFold is a local-first Android EPUB reader built with Jetpack Compose and the Readium Kotlin toolkit.
The current product loop is:

1. Import an `.epub` from the system picker or Android's Open with flow.
2. Copy it into app-managed storage.
3. Show it on a warm editorial-style home shelf.
4. Open it in a fragment-hosted Readium EPUB reader.
5. Persist reading progress and resume from the last saved locator.

Keep the app focused on this loop unless the user explicitly asks for broader scope.

## Current Repo State

- Single-module Android app in `app/`.
- Package root is `com.shubhamghanmode.inkfold`.
- Home UI is Compose in `feature/home/` with an adaptive lazy shelf grid, a recent-imports row, and explicit per-book actions.
- External EPUB intent handling lives in `feature/importer/ImportActivity.kt`.
- Readium reader hosting lives in `feature/reader/` and remains fragment-based on purpose, with a Compose overlay for chrome and settings.
- Reader appearance persistence lives in `feature/reader/preferences/` using DataStore-backed Readium `EpubPreferences`.
- Reader outline metadata and TOC/page-list/landmark mapping live in `feature/reader/outline/`.
- Reader read-aloud support and TTS preference persistence live in `feature/reader/tts/`.
- Persistence uses Room for books and reading position metadata.
- App-managed copies live under `filesDir/library/books/` and extracted covers live under `filesDir/library/covers/`.
- Imports are deduplicated by SHA-256 content hash.
- Build uses core library desugaring because Readium requires it.
- App theme colors now support eight persisted Material 3-derived preset palettes from the home-screen settings sheet.
- Classic Shelf, Redwood Binding, and Amber Cloth were regenerated from distinct Material 3 seeds so the warm presets now separate into leather-brown, maroon-plum, and true-amber families instead of clustering around the same terracotta range.
- Fresh installs and missing/invalid app-theme preference values now default to the Iron Ledger palette preset.
- Compose home UI and the fragment/XML reader both follow the system light/dark setting within the selected InkFold palette preset.
- Reader status-bar icon contrast now follows the active reader appearance theme, with light icons for the dark reader theme and dark icons for light or sepia.
- Reader appearance settings now support shared EPUB defaults for theme, paged-versus-scroll mode, bounded font size, typeface, dark-theme image filters, and discrete page margins.
- The sepia reader option now uses InkFold-specific Readium color overrides with background `#FFECC3` and content `#121212` instead of the bundled Readium sepia defaults.
- Reader chrome is now an overlaid Compose layer with a hideable top bar and anchored settings sheet instead of a layout-consuming toolbar row.
- Reader chrome now also includes a bottom progress sheet with a quick-jump slider plus an outline sheet for Contents, Pages, and Landmarks when the publication exposes them.
- Reader read aloud now ships through Readium TTS with compact transport controls, spoken-text highlight, throttled visual follow-along, and separate speed/pitch/language/voice settings.
- Custom reader font assets are served from `app/src/main/assets/fonts/` through `ReaderNavigatorConfiguration`.
- Page-flip work is scaffolded only through `PageFlipCoordinator`, `PageFlipOverlay`, and `PageSnapshotter`; no interactive page-flip feature ships yet.
- Shelf cover fallbacks are rendered directly in Compose; do not route layered XML drawables such as `layer-list` placeholders through Compose `painterResource()`.
- Verified on `2026-03-31`:
  - `:app:assembleDebug` passes.
  - `:app:testDebugUnitTest` passes.
  - `:app:assembleDebugAndroidTest` passes.
  - `:app:lintDebug` passes.
  - No emulator/device was available for `:app:connectedDebugAndroidTest`, manual reader acceptance, or file-explorer acceptance.

## Implemented Architecture

Current structure:

```text
app/src/main/java/com/shubhamghanmode/inkfold/
  AppContainer.kt
  InkFoldApplication.kt
  ReadiumServices.kt
  data/book/
  feature/home/
  feature/importer/
  feature/reader/
  feature/reader/outline/
  feature/reader/preferences/
  feature/reader/tts/
  ui/theme/
```

Responsibilities:

- `InkFoldApplication` - app entrypoint exposing the app-scoped container.
- `AppContainer` - lightweight manual dependency graph; do not replace with Hilt unless explicitly requested.
- `ReadiumServices` - owns `AssetRetriever` and `PublicationOpener`.
- `data/book/` - Room entities/DAO plus import, file-storage, deletion, and progression persistence logic.
- `feature/home/` - adaptive shelf UI, continue-reading hero, recent imports rail, explicit book action menus, import picker entry, transient message handling, and app-palette settings sheet.
- `feature/importer/` - no-UI trampoline activity for Android `VIEW`/`SEND` EPUB intents.
- `feature/reader/` - reader session prep, shared navigator configuration, `ReaderActivity`, `ReaderFragment`, hideable overlay chrome, bottom progress sheet navigation, locator persistence, and page-flip scaffolding.
- `feature/reader/outline/` - publication navigation mapping plus UI-safe Contents/Pages/Landmarks models and sheet rendering.
- `feature/reader/preferences/` - shared EPUB appearance persistence, Readium preference filtering, normalization helpers, settings-sheet UI, and theme previews.
- `feature/reader/tts/` - Readium TTS init data, controls, DataStore-backed TTS preferences, missing-voice recovery prompts, and playback/error coordination.
- `ui/theme/` - InkFold preset registry, generated palette data, DataStore-backed app palette persistence, typography, and Compose theme that follows system dark mode instead of forcing light mode.

## Product Scope

### In Scope

- Local EPUB import.
- Android Open with support for EPUB files.
- Personal library shelf UI.
- Fragment-hosted Readium EPUB reading.
- Persisted reading progression and resume.
- Offline usage for imported local books.

### Out Of Scope Unless Explicitly Requested

- PDF, CBZ, comics, or audiobook support.
- Accounts, sync, remote backup, or cloud libraries.
- OPDS browsing in InkFold itself.
- DRM/LCP work in the app.
- Highlights, annotations, search UI, or bookstore features.

## Readium Integration Rules

- Keep Readium wiring inside `ReadiumServices`, `ReadiumPublicationInspector`, and `feature/reader/`.
- Do not leak `Publication`, navigator factories, or `Locator` handling into general Compose UI code.
- Keep the reader hybrid: Compose for app chrome, fragment-hosted `EpubNavigatorFragment` for actual EPUB rendering.
- Keep `ReaderViewModel` activity-scoped with its explicit book-id factory. Any fragment sharing it must use the same factory/creation extras instead of relying on the default Android `ViewModelProvider` factory.
- Keep navigator-level configuration inside `ReaderNavigatorConfiguration`, including served reader font assets and scroll-mode input behavior.
- Keep TOC/page-list/landmark mapping inside `feature/reader/outline/` and expose only UI-safe models to Compose.
- Keep read-aloud integration inside `feature/reader/tts/`; start TTS from the current visible locator and keep follow-along jumps throttled.
- Keep EPUB appearance persistence inside `feature/reader/preferences/` and use Readium preference APIs rather than custom CSS injection.
- Keep the custom InkFold sepia reader treatment implemented through Readium `backgroundColor` / `textColor` preference overrides paired with `Theme.SEPIA`, rather than trying to invent a fourth Readium `Theme` enum value.
- The current settings UI edits shared EPUB defaults only. The storage layer is ready for per-book overrides, but the UI should stay shared-defaults-first unless explicitly expanded.
- Keep TTS preferences separate from EPUB appearance preferences.
- Use the bundled sample app in `readium-kotlin-toolkit-3.1.2/test-app/` as the primary implementation reference.
- Prefer the sample's import, publication opening, and reader session patterns over inventing a new architecture.

## Storage And Import Model

- Import through `OpenDocument` in-app or `ImportActivity` for external intents.
- Copy every accepted EPUB into app storage immediately.
- Do not depend on long-term SAF URI persistence for library access in the current design.
- Deduplicate books by SHA-256 content hash.
- Store extracted cover images separately from the EPUB file.
- Persist:
  - title
  - author
  - identifier
  - media type
  - app-managed file path
  - cover path
  - locator JSON
  - normalized progression percent
  - import timestamp
  - last-opened timestamp

## UI Guidance

- Preserve the current editorial shelf direction: parchment tones, dark ink text, serif-forward typography, soft elevation.
- The home screen should feel like a personal library, not a storefront or generic dashboard.
- Keep book covers tall at roughly `2:3`.
- Continue-reading state should remain visually prominent above the fold.
- Keep the full library rendering lazy and adaptive; avoid measure-all shelf layouts as the library grows.
- Keep destructive actions visible but restrained, such as a calm overflow menu on book cards instead of relying only on long-press.
- Keep missing or unreadable cover fallbacks in Compose or vector/raster drawables that Compose can load safely.
- Honor system dark mode within the selected InkFold palette preset; do not switch to generic dynamic colors unless explicitly requested.
- Avoid busy motion and avoid decorative chrome inside the reader.
- Reader chrome should stay overlaid and hideable so it does not permanently consume reading space.
- Reader quick navigation should stay compact and progression-based because EPUB pagination shifts with font and margin changes.
- Keep TOC access inside the bottom progress chrome rather than growing a second navigation surface.
- Keep read-aloud controls compact and visually subordinate to the page; settings should stay in a separate sheet from visual appearance controls.
- Keep theme-preset browsing curated and readable now that the app ships eight presets.
- If new reader settings are added later, prefer calm utility over feature sprawl.

## Build And Dependency Rules

- Keep dependency versions in `gradle/libs.versions.toml`.
- Keep manual edits in Gradle Kotlin DSL.
- Room currently uses KSP.
- Readium currently requires:
  - core library desugaring enabled in `app/build.gradle.kts`
  - `android.newDsl=false`
  - `android.builtInKotlin=false`
- Read aloud currently depends on `org.readium.kotlin-toolkit:readium-navigator-media-tts`.
- Keep the Android manifest query for `android.intent.action.TTS_SERVICE` unless the Readium TTS integration is explicitly removed.
- Those AGP flags are a compatibility workaround on this branch; do not remove them casually without re-verifying Room/KSP and the full app build.
- Add new dependencies only when they materially reduce complexity or are required for testing/verification.

## Testing And Verification

Use these as the default verification set for meaningful changes:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebugAndroidTest`
- `./gradlew :app:lintDebug`

When a device or emulator is available, also run:

- `./gradlew :app:connectedDebugAndroidTest`

Manual acceptance to prefer when available:

1. Import an EPUB from the in-app picker.
2. Confirm it appears on the shelf once.
3. Open it, move forward, leave the reader, and reopen it.
4. Confirm resume starts near the saved position.
5. Open the same EPUB from a file explorer and confirm InkFold appears in the Open with sheet.
6. Confirm opening the same file again does not create a duplicate library row.
7. In the reader, open settings and confirm theme, scroll mode, font size, typeface, image filter, and page margins restyle the current page live when applicable.
8. Leave the reader and reopen the same book.
9. Confirm appearance settings persist and resume location is still correct.
10. Open the bottom progress chrome and confirm the quick-jump slider still moves to the requested point without breaking saved progression.
11. Open the outline sheet and confirm Contents, Pages, and Landmarks appear only when present and navigate to the selected entry.
12. Start read aloud from the current page and confirm highlight, follow-along progression, play/pause/stop, and TTS settings changes work.
13. If device voice data is missing, confirm the install-voice prompt is recoverable.
14. Open app settings on the home shelf and switch palette presets; confirm the shelf and reader chrome restyle in both light and dark system mode.

## Next Priorities

- Run connected instrumentation tests on a real emulator/device.
- Perform the full external file-open acceptance flow from a file manager.
- Run on-device manual acceptance for the live reader settings, app palettes, progress-slider flow, TOC navigation, read-aloud flow, and missing-voice install prompt.
- Decide whether to expose per-book appearance overrides after the shared-defaults experience proves stable with the expanded settings surface.
- Decide whether read aloud should grow background/media-session controls beyond the current in-reader experience.
- Keep page-flip investigation behind the current reader roadmap instead of ahead of it.
- Add bookmarks/highlights/search only after the core read/import/resume loop remains stable.

## Documentation Maintenance

- Update this file whenever architecture, storage model, build constraints, or verification status changes.
- Update `README.md` when setup, status, or contributor-facing behavior changes.
- `README.md` is now a presentation-grade landing page with repo-native SVG artwork, setup guidance, verification tables, and product/scope sections; preserve that level of detail and visual structure when refreshing it.
- Keep `IMPROV.md` aligned with the latest agreed roadmap status, including shipped reader ergonomics/accessibility features and any remaining validation follow-up.
- Keep guidance specific to InkFold and its current implementation rather than generic Android advice.
- If a task only changes external tooling or agent skills outside this repo, keep InkFold product guidance intact and only update this file when the contributor workflow itself needs clarification.
