# Aurora Music

A premium, local-first Android music player built in Kotlin with Jetpack Compose,
implementing the Phase 1 specification in
[`aurora-music-master-prompt.md`](aurora-music-master-prompt.md).

**No accounts. No login. No cloud sync. Ever.** Your library, playlists,
favourites, history and settings never leave the device.

---

## Status

Phase 1 (local-library playback, end to end) is implemented. The architecture is
deliberately source-agnostic so the Phase 2 catalog/streaming layer can be added
additively — see [Phase 2 readiness](#phase-2-readiness) below.

> **CI note:** the build workflow is committed at **`ci/android-build.yml`** and
> must be copied to `.github/workflows/` once to activate. See
> [`SETUP.md`](SETUP.md) for the one-line command and why.

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 (no XML views) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Concurrency | Coroutines + Flow / StateFlow |
| Navigation | Navigation Compose |
| Database | Room (explicit migrations, no destructive fallback) |
| Preferences | DataStore |
| Media engine | AndroidX Media3 + ExoPlayer |
| Images | Coil |
| Background work | WorkManager |
| Networking | Retrofit + OkHttp (Phase 2 scaffold, unused in Phase 1) |

- **minSdk 33** (Android 13), **targetSdk / compileSdk 35**, JDK 17.

## Architecture

```
domain/          MediaItem, MusicRepository, SettingsRepository  (pure Kotlin)
data/
  database/      Room entities + DAOs
  mediastore/    MediaStore scanner
  preferences/   DataStore-backed settings
  repository/    LocalMusicRepository (Phase 1 implementation)
  network/       CatalogApi — Phase 2 scaffold, keyless & anonymous
  scanner/       LibraryScanWorker (WorkManager)
player/          PlayerManager (single source of truth) + PlaybackService
core/
  common/        dispatchers, formatters, fuzzy search
  designsystem/  theme, Contour Glow, shared components
feature/         home, discover, search, library, settings, player, onboarding
navigation/      destinations + NavHost
```

Three rules keep the layering honest:

1. **One playable model.** `MediaItem` carries a `source` (`LOCAL` /
   `REMOTE_CATALOG`) and resolves its own `playbackUri`. There is no `LocalSong`.
2. **UI never touches Room.** Every screen goes through the `MusicRepository`
   interface, so swapping in a merged local+catalog repository is a one-line
   change in `RepositoryModule`.
3. **One player instance.** `PlayerManager` wraps a `MediaController` bound to
   `PlaybackService`; nothing else constructs a player.

## Implemented features

**Home** — time-based greeting, Continue Listening, Recently Played / Added,
Favourites (songs, albums, artists), Most Played, Random Picks, local
recommendations, 7 mood collections, quick actions, and a first-run "Let's find
your music" empty state.

**Discover** — 13 category tiles, Hidden Gems, Lossless Collection, Highest
Quality Audio, Recently Imported, album/artist/genre browsing.

**Search** — debounced instant search, typo-tolerant fuzzy matching
(Levenshtein + prefix/word-boundary scoring), voice search, recent searches, and
results grouped into Songs / Albums / Artists / Playlists / Folders with filters.

**Library** — 7 tabs (Songs, Albums, Artists, Genres, Playlists, Folders,
Favourites), 7 sort orders, playlist create/rename/delete/pin, multi-select
actions, and grid/list layouts.

**Player** — ~60% artwork, dynamic gradient background, portrait *and* landscape
layouts, scrubbing seek bar with buffered progress, gapless playback, repeat /
shuffle modes, 7 playback speeds, sleep timer with a 10-second fade-out, queue
management (reorder, remove, save as playlist), full-screen synced lyrics
(`.lrc`), and gestures: swipe for next/previous, swipe-down to minimise,
double-tap to favourite, drag-up on the mini player to expand.

**Equalizer** — 10 bands (31 Hz – 16 kHz) as vertical faders, 13 built-in
presets, bass/treble boost, virtualizer, balance, preamp and limiter.

**Settings** — theme (Light / Dark / AMOLED / System), Material You or
artwork-derived accent, animation level, Contour Glow toggle, playback, library,
notification and privacy options.

**Playback service** — foreground `MediaSessionService` with lock-screen and
Bluetooth controls, audio-focus handling, becoming-noisy handling, and
notification channels created before the permission prompt.

## Design language

ColorOS-inspired rather than a Spotify clone:

- **Contour Glow** — soft gradient edge lighting on cards and panels
  (`Modifier.contourGlow()`), user-disableable for battery and motion sensitivity.
- **Glass surfaces** — translucent layered gradients (`Modifier.glassSurface()`).
- Generous rounding (8–36 dp), edge-to-edge layouts, large artwork, full
  Material 3 type scale, and dynamic colour from album art.

## Phase 2 readiness

Everything Phase 2 needs is already in place and unused:

- `MediaSource.REMOTE_CATALOG` and `MediaItem.streamUrl` — covered by a unit test
  asserting the player resolves a remote item without any UI change.
- `network/CatalogApi` — Retrofit scaffold with **no auth interceptor**, by design.
- `MusicRepository` — add `CatalogMusicRepository` + `MergedMusicRepository`, then
  rebind in `RepositoryModule`.

Hard constraint carried forward: any future catalog provider must work
anonymously or with a static app-level key. A provider requiring per-user login
is the wrong provider, not a reason to add accounts.

## Building

```bash
./gradlew :app:assembleDebug     # debug APK
./gradlew :app:testDebugUnitTest # unit tests
./gradlew :app:assembleRelease   # release APK (R8 + resource shrinking)
```

Release builds fall back to the debug signing key when no keystore is
configured, so CI always produces an installable artifact. See
[`SETUP.md`](SETUP.md) for proper signing.

## Not yet implemented

Tracked against the spec for transparency: home-screen widgets (Glance), Quick
Settings tile, Android Auto browse tree, backup/restore UI, metadata editor
screen, tag writing to files, crossfade audio processor (the setting persists but
isn't yet applied to the renderer), and the hidden/private collection lock.
