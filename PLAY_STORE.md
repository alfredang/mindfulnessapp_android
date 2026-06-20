# Play Store submission checklist

This produces and submits the signed Android App Bundle for **Tertiary Mindfulness**
(`com.alfredang.mindfulnesspractice`).

> **Current status (Jun 20, 2026):** the app was created in the Google Play Console and the
> closed-testing release **was submitted — Update status = "In review"** (typically up to 7 days).
> All app-content declarations, the data-safety form, content rating, store listing, store
> settings, countries, and tester lists (All Testers + Android reviewers) are complete. The steps
> below document that flow for future updates. App status stays **"Draft"** until the first
> release passes review and goes live — then it flips to **"Closed testing."**

## 0. What's already done in this repo

- ✅ Signed **release AAB** built at `app/build/outputs/bundle/release/app-release.aab`
- ✅ **Upload keystore** generated (`upload-keystore.jks`, gitignored) and wired via `keystore.properties`
- ✅ **Adaptive launcher icon** (all densities) + **512×512 Play icon** (`playstore-icon-512.png`)
- ✅ **Feature graphic** `feature-graphic.png` (1024×500) + **phone screenshots** `store_shot_1.png`, `store_shot_2.png` (9:16)
- ✅ **Privacy policy** hosted at <https://alfredang.github.io/mindfulnessapp_android/privacy.html> (`docs/privacy.html`)

> ⚠️ **Back up `upload-keystore.jks` and its passwords** somewhere safe. If you lose the upload key
> you must contact Google to reset it. The passwords are in `keystore.properties` (gitignored).

## 1. Rebuild the bundle (if needed)

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

## 2. Create the app in Play Console

1. Go to <https://play.google.com/console> → **Create app**.
2. App name **Tertiary Mindfulness**, language English, **App**, **Free**.
3. Accept the Play developer agreements.

## 3. Upload the bundle

- **Release → Testing → Internal testing → Create new release** (start here, not Production).
- Let Google **enrol in Play App Signing** (recommended) — you upload with the upload key, Google
  re-signs with the managed app key.
- Drop in `app-release.aab`. Add release notes. **Save → Review → Roll out**.

## 4. Store listing & required declarations

- **Main store listing:** short + full description, app icon (`playstore-icon-512.png`), a feature
  graphic (1024×500), and ≥2 phone screenshots (`screenshot.png` is one; capture a few more).
- **Content rating:** complete the questionnaire → this app is **Everyone** (a meditation
  audio player; no objectionable content).
- **Data safety:** declare **no data collected, no data shared** — the app has no account, no
  network, no analytics. Everything plays locally.
- **App access:** all functionality is available without an account → "All functionality available
  without special access".
- **Target audience & ads:** no ads; pick the appropriate audience age groups.
- **Privacy policy:** Play requires a URL even for no-data apps — host a short one (the iOS app's
  privacy page text can be reused).

## 5. Submit for review

Once the internal-testing release is verified, promote it to **Closed/Open testing** and then
**Production → Create release → Review → Roll out**. Google review typically takes a few hours to a
few days.

## Notes specific to this app

- **Phone-only, portrait, offline.** No account system → no account-deletion data-safety obligation.
- `targetSdk = 35` satisfies the current Play target-API requirement.
- `applicationId = com.alfredang.mindfulnesspractice`, `versionCode = 1`, `versionName = "1.0"`.
  Bump `versionCode` in `app/build.gradle.kts` for every subsequent upload.
