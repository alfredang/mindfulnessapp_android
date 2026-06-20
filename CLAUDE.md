# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this app is

A native **Android** (Jetpack Compose) port of the iOS app
[alfredang/mindfulnessapp](https://github.com/alfredang/mindfulnessapp). One screen
("Mindfulness Practice") plays a bundled guided-meditation track
(`app/src/main/res/raw/mindfulness_practice.m4a`, a soothing female voiceover) behind a still
zen-garden image (`app/src/main/res/drawable/practice_zen.jpg`), with a scrubber and
Start / Pause / Stop transport. Two extra controls: a **session length** picker (5/10/15/20 min,
drives auto-stop) and **Background Music** (pick an audio file from the device via the Storage
Access Framework, looped quietly under the voice at volume 0.22). No login, no backend, no data
collection — everything plays locally.

## Build & run

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"   # JDK 17+
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:bundleRelease          # signed release AAB (needs keystore.properties)
```

- Versions: AGP 8.7.3, Kotlin 2.0.21, Gradle 8.11.1, Compose BOM 2024.10.01, compileSdk/targetSdk 35, minSdk 26.
- The SDK path is in `local.properties` (gitignored). Source of truth for version/build/appId is
  `app/build.gradle.kts` (`versionName`, `versionCode`, `applicationId`).
- No third-party libraries beyond AndroidX/Compose. No tests or lint config.

## Architecture (mirrors the iOS app one-to-one)

Four Kotlin files under `app/src/main/java/com/alfredang/mindfulnesspractice/`:

- **MainActivity.kt** — `ComponentActivity`; `enableEdgeToEdge()` then `setContent { PracticeScreen() }`.
- **PracticeScreen.kt** — the entire Compose UI (teal header, full-bleed `practice_zen`, length +
  Background Music pills, scrubber, transport). Owns a `PracticeViewModel` via `viewModel()`;
  mirrors `currentTime` into a local `scrubValue` gated by `isScrubbing`. The music pill uses the
  SAF `ACTION_OPEN_DOCUMENT` picker (`rememberLauncherForActivityResult`). Styling from `Theme`.
- **PracticeViewModel.kt** — `AndroidViewModel` with a `MediaPlayer` for the bundled voice `.m4a`
  plus an optional looping `MediaPlayer` for background music (volume 0.22). Session timing is
  wall-clock based (`anchorElapsedMs`/`anchorClock` over `SystemClock.elapsedRealtime`) on a 0.2s
  coroutine ticker, so a chosen `sessionLength` longer than the voice keeps music going to the end
  and shorter cuts it off. `selectSessionLength`, `setBackgroundMusic`, `clearBackgroundMusic`.
  (Named `selectSessionLength`, not `setSessionLength`, to avoid clashing with the generated
  property setter for `sessionLength`.)
- **Theme.kt** — all colors (dark teal palette). Mirrors iOS `Theme.swift`. Change the look here.

## Play Store

Release builds are signed with an upload keystore via a gitignored `keystore.properties`
(`storeFile=../upload-keystore.jks`). Upload `app/build/outputs/bundle/release/app-release.aab`
to the Play Console and enrol in Play App Signing. Full checklist in `PLAY_STORE.md`.
Phone-only, no account system — no account-deletion data-safety obligation.
