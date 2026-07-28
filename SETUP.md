# Aurora Music — build & CI setup

## Enabling the APK build on GitHub Actions

The build workflow lives at **`ci/android-build.yml`**. It is not at
`.github/workflows/` because the agent's GitHub App token lacks the `workflows`
permission, so pushes containing that path are rejected by GitHub.

To activate CI, copy the file once:

```bash
mkdir -p .github/workflows
git mv ci/android-build.yml .github/workflows/android-build.yml
git commit -m "ci: enable Android build workflow"
git push
```

(Or paste the file's contents into a new `.github/workflows/android-build.yml`
via the GitHub web UI — **Add file → Create new file**.)

## What the workflow does

| Step | Command |
|---|---|
| Debug APK | `./gradlew :app:assembleDebug` |
| Unit tests | `./gradlew :app:testDebugUnitTest` |
| Lint | `./gradlew :app:lintDebug` (non-blocking) |
| Release APK | `./gradlew :app:assembleRelease` (R8 + resource shrinking) |

Both APKs are uploaded as build artifacts (`aurora-music-debug-apk`,
`aurora-music-release-apk`), retained for 30 days.

## Release signing (optional)

If no signing config is present, the release APK is signed with the debug key so
CI always produces an installable artifact. To sign properly, either commit a
`keystore.properties` at the repo root:

```properties
storeFile=release.jks
storePassword=...
keyAlias=aurora
keyPassword=...
```

…or set these repository secrets and expose them as environment variables in the
workflow: `AURORA_KEYSTORE_FILE`, `AURORA_KEYSTORE_PASSWORD`,
`AURORA_KEY_ALIAS`, `AURORA_KEY_PASSWORD`.

## Building locally

Requires JDK 17 and the Android SDK (compileSdk 35, minSdk 33).

```bash
./gradlew :app:assembleDebug
```
