# InkFold Reader Roadmap Status

Updated on `2026-03-29`.

The reader roadmap that previously lived in this file has now been implemented in the current branch. This document now tracks what shipped and what still needs validation or follow-up.

## Delivered In This Slice

1. Read aloud support powered by Readium TTS.
   - starts from the current visible locator
   - highlights the spoken utterance
   - keeps the visual reader roughly in sync with throttled follow-along jumps
   - exposes speed, pitch, language, and voice controls
   - prompts for Android voice-data installation when required
2. Vertical scroll mode for reflowable EPUBs alongside paged mode.
3. Normalized text-size controls with a `50%..250%` range plus increment/decrement actions.
4. Discrete page-margin presets with plus/minus controls.
5. Typeface selection for `Original`, `Literata`, `Serif`, `Sans Serif`, `IA Writer Duospace`, `Accessible DfA`, and `OpenDyslexic`.
6. Dark-theme image filter options for `Original`, `Darken`, and `Invert`.
7. Bottom-chrome outline access for `Contents`, `Pages`, and `Landmarks` when present.
8. Expanded app-palette support with eight persisted InkFold presets generated from Material 3 color roles.
9. Shared reader cleanup work that supports the above:
   - split reader UI state by appearance, navigation, and TTS concerns
   - centralized navigator configuration in `ReaderNavigatorConfiguration`
   - added preference normalization helpers
   - added `feature/reader/outline/` and `feature/reader/tts/`

## Guardrails That Still Apply

- Stay inside InkFold's current core loop: import, shelf, read, resume.
- Keep the reader hybrid: Compose overlay chrome with fragment-hosted `EpubNavigatorFragment`.
- Keep shared-default appearance settings as the default model unless a later task explicitly expands to per-book overrides.
- Keep Readium-specific publication, locator, outline, and TTS wiring inside `feature/reader/`.
- Keep the reader calm. New work should favor compact utility over feature sprawl.

## Remaining Follow-Up Work

- Run `./gradlew :app:connectedDebugAndroidTest` on a real emulator or device.
- Perform full manual acceptance for:
  - in-app EPUB import
  - external `Open with` flow from a file manager
  - paged-versus-scroll switching
  - typeface, image filter, text-size, and page-margin restyling
  - TOC/page-list/landmark navigation
  - read-aloud playback, settings changes, and missing-voice recovery
  - app-palette switching in both light and dark system mode
- Decide whether shared-default appearance settings are stable enough to justify per-book overrides.
- Decide whether read aloud needs background/media-session controls beyond the current in-reader experience.
- Keep page-flip investigation behind the validation and polish work above.

## Verification Status

Verified on `2026-03-29`:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:assembleDebugAndroidTest`
- `./gradlew :app:lintDebug`

`adb devices` showed no connected emulator or device at verification time, so connected instrumentation and on-device manual acceptance remain pending.
