# AI Development Master Prompt — Aurora Music (Kotlin Music App)

**Project title (working name):** Aurora Music
**Target platform:** Android 13+ (min SDK 33), Kotlin, Jetpack Compose only
**Type:** Local-first music player, architected to support a remote catalog/streaming layer added after core app completion — **no user accounts, no login, no cloud auth, ever**

> **Phasing note:** Phase 1 (this build) ships local-library playback end-to-end. Phase 2 (later, separate work) adds a remote catalog fetch + streaming source alongside local files. The architecture below (repository interfaces, `MediaItem` model, network module) must be built now so Phase 2 is a plug-in, not a rewrite. Do not hardcode "local file" assumptions into shared layers — see Section 2a and Section 12.

---

## 0. Mission Statement

Build a premium Android music player in Kotlin using Jetpack Compose. The app should feel like it ships with a flagship Android phone — inspired by the elegance and smoothness of ColorOS, with functionality that rivals or exceeds Spotify. It plays local on-device music today, and is architected to add remote catalog/streaming playback later, without requiring any user account, sign-in, or cloud authentication at any phase. Every screen, animation, gesture, transition, and interaction must feel polished, premium, and consistent.

**Final development rule:** Do not build a basic music player. The goal is a flagship-level Android music ecosystem that combines the beauty of Apple Music, the functionality of Spotify, and the customization freedom of Android — with local playback first and a streaming catalog layered in later, all without accounts.

**Priority order:**
1. User experience
2. Performance
3. Stability
4. Local + future-streaming ownership (no accounts, no cloud login — ever)
5. Premium design

---

## 1. Core Principles

The application must be:
- Local-first now, streaming-ready later (Phase 2 plugs into the same player/queue/library models)
- Fast, stable, battery-efficient
- Smooth at 60 Hz, 90 Hz, and 120 Hz refresh rates
- Optimized for Android 13–16
- Built entirely in Kotlin, using Jetpack Compose only (no XML views)
- Modular and scalable
- Built with Clean Architecture, maintainable long-term
- Designed for phones, tablets, and foldables
- Production-ready from day one (not a prototype)
- **No user accounts or authentication at any phase** — a future catalog fetch must work anonymously (public/keyless API, or app-level API key — not user login)

---

## 2. Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 (customized with ColorOS-inspired design tokens) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Concurrency | Kotlin Coroutines + Flow |
| State | StateFlow |
| Navigation | Navigation Compose |
| Database | Room (structured data) |
| Preferences | DataStore |
| Media engine | AndroidX Media3 + ExoPlayer |
| Image loading | Coil |
| Background tasks | WorkManager |
| Permissions | Android Runtime Permissions, Android 13+ granular media permissions |
| Networking (Phase 2, scaffolded now) | Retrofit + OkHttp for future catalog/streaming fetch — anonymous/keyless calls only, no login |
| Animation | Compose Animation APIs, Shared Element transitions where appropriate |
| Testing | JUnit, Espresso, Compose UI Tests |

### Application module structure
```
app/
core/
common/
ui/
player/
library/
albums/
artists/
playlists/
search/
home/
discover/
settings/
downloads/
equalizer/
widgets/
notifications/
database/
repository/
domain/
network/          (Phase 2: remote catalog + streaming source; keyless/anonymous only — no auth, no accounts)
utilities/
```
Every feature should be isolated in its own module where practical.

### 2a. Repository abstraction (build this now — critical for Phase 2)

To avoid a rewrite when the catalog/streaming layer lands, every data-facing layer must go through an interface, not a concrete local implementation:

- **`MediaItem`** — a single domain model representing a playable track, regardless of origin. Include a `source` field (`LOCAL`, `REMOTE_CATALOG` — extensible enum) and a nullable `remoteId` / non-nullable `localUri` as appropriate, rather than two separate models.
- **`MusicRepository` interface** — exposes songs/albums/artists/playlists as `Flow`s. Phase 1 ships `LocalMusicRepositoryImpl` (Room + MediaStore). Phase 2 adds `CatalogMusicRepositoryImpl` (Retrofit) and a `MergedMusicRepositoryImpl` that combines both — the ViewModels and UI should never know which implementation is active.
- **`PlayerManager`** must accept a `MediaItem` and resolve its playable URI internally (local file path vs. remote stream URL) — the player screen, queue, and notification code must not branch on source type.
- **Search, Discover, and Home sections** should be written against `MusicRepository`, not against Room DAOs directly, so Phase 2 can inject catalog results without touching UI code.
- **No auth hooks anywhere in this abstraction.** The catalog repository must be designed to hit a public/anonymous or app-scoped-API-key endpoint — never a per-user login, OAuth flow, or account system. If the eventual catalog API requires user auth, that requirement conflicts with this spec and must be flagged, not silently implemented.

---

## 3. Design Philosophy

**Do not imitate Spotify's appearance directly.** Instead, create a premium identity inspired by **current** ColorOS design language (ColorOS 16 shipping now / ColorOS 17's "Liquid Glass" direction rolling out later in 2026) — not the older ColorOS 13/14 "Aquamorphic" look:
- Transparent, glass-inspired surfaces with a **Contour Glow** treatment — soft edge lighting/highlight on card and panel borders rather than flat frosted panels; provide a toggle to disable it for battery/motion-sensitivity
- Rounded corners, floating cards, soft shadows
- Edge-to-edge layouts
- Minimal iconography
- Large album artwork
- Smooth, "parallel" animations — multiple surfaces (mini player, queue sheet, now-playing) should be able to animate simultaneously without stutter, rather than blocking on each other
- Dynamic colors extracted from album art
- High-quality typography
- Spacious layouts, elegant spacing
- Rich micro-animations
- Natural light/shadow motion cues on transitions (subtle, not skeuomorphic)

> **Design note:** ColorOS's visual language moves fast (roughly one refresh per year, with incremental point releases). Treat "Contour Glow" and "Liquid Glass/liquid acrylic surfaces" as the current reference points, but re-check OPPO's design documentation shortly before final visual polish, since this will drift further before ship.

### Color system
Supports: Light Theme, Dark Theme, AMOLED Black, Dynamic Material You colors, manual accent color selection.

Album artwork dynamically influences:
- Player background
- Notification colors
- Widget colors
- Lock screen colors (where supported)

### Typography scale
`Display Large / Medium / Small`, `Headline Large / Medium / Small`, `Title Large / Medium`, `Body Large / Medium / Small`, `Label Large / Medium`.
Text should never truncate unexpectedly — use marquee scrolling only where appropriate.

### Navigation (bottom nav, 5 tabs)
1. Home
2. Discover
3. Search
4. Library
5. Settings

Mini Player always appears above Bottom Navigation whenever music is active.

---

## 4. Home Screen

**Purpose:** Provide instant access to music.

**Greeting header:** Good Morning / Good Afternoon / Good Evening (time-based).

**Sections:**
- Continue Listening
- Recently Played
- Recently Added
- Favourite Songs
- Favourite Albums
- Favourite Artists
- Downloaded Songs
- Pinned Playlists
- Most Played
- Random Picks
- Recommended from Local Library
- Mood Collections: Relax, Workout, Focus, Sleep, Travel, Party, Romance

**Quick Actions:** Shuffle All, Resume Playback, Scan Device, Equalizer, Sleep Timer.

**Missing detail added — empty state:** If the library has never been scanned, Home shows a first-run card: "Let's find your music" with a **Scan Device** CTA in place of all sections above.

---

## 5. Discover

**Purpose:** Help users explore their music library in meaningful ways. In Phase 1 this is local-library discovery only; in Phase 2 this screen is where catalog-sourced recommendations/new releases will surface alongside local picks — design the section list as data-source-agnostic (a section doesn't need to care whether its items are local or remote).

**Categories:** Genres, Artists, Albums, Years, Folders, Recently Imported, Highest Rated, Longest Songs, Shortest Songs, Highest Quality Audio, Lossless Collection, Recently Downloaded, Smart Playlists, Hidden Gems (tracks rarely played but highly rated/favourited).

---

## 6. Search

**Features:** Instant search, real-time filtering, voice search.

**Search by:** Song, Artist, Album, Genre, Folder, Playlist, Composer, File Name.

**Support:** Recent searches, search suggestions, fuzzy matching, typo correction, results grouped into sections (Songs / Albums / Artists / Playlists / Folders).

---

## 7. Library

**Contains:** Songs, Albums, Artists, Genres, Folders, Playlists, Favourites, Downloads, History, Recently Added, Most Played, Hidden Music, Private Collection.

### Folder Browser
- Support: Internal Storage, SD Card, USB OTG
- Users can browse music directly by folder without importing everything
- Folder artwork generated automatically

### Supported formats
MP3, AAC, M4A, FLAC, ALAC, WAV, AIFF, OGG, Opus, APE, DSF (optional), DFF (optional).
Unsupported files must display a friendly error instead of crashing.

### Media Scanner
Automatically detects: new songs, deleted songs, renamed songs, moved songs, changed metadata, duplicate files.
Runs efficiently in the background using WorkManager; only re-indexes changed media.

### Local storage model
No account system. No online login. Everything stored locally using:
- Room database — structured data (playlists, favourites, history, metadata overrides)
- DataStore — preferences (theme, equalizer presets, playback settings)
- Device file system — the actual music files
- Encrypted local storage where appropriate for app-specific data

---

## 8. Audio Engine, Player System, Playback, Queue, Equalizer & Device Integration

### Core audio philosophy
The music engine is the heart of the application. Playback must feel instant, stable, and premium.

**Target startup time:** local playback starts in under 150 ms where possible.

Playback must continue smoothly while:
- Screen is off
- Device is locked
- User switches apps
- User receives notifications
- User uses split-screen
- User connects/disconnects Bluetooth
- User connects/disconnects wired headphones
- Device rotates
- Theme changes
- Music library rescans

The player must never restart unexpectedly or lose the queue.

### Media engine
Use AndroidX Media3 + ExoPlayer. Implement a dedicated `PlayerManager` as the single source of truth.

**Responsibilities:** audio decoding, queue management, playback state, metadata updates, notification updates, `MediaSession` integration, audio focus handling, playback persistence, equalizer integration, sleep timer, playback statistics.
Never create multiple player instances.

### Player Service
Implement a foreground `MediaSessionService`.

**Responsibilities:** background playback, lock screen controls, Android Auto support, Bluetooth controls, headset controls, notification controls, voice assistant integration, media browser compatibility. Playback must continue after the UI closes.

### Playback states
Support every state: Idle, Loading, Buffering, Preparing, Playing, Paused, Stopped, Ended, Error, Seeking, Fast Forwarding, Rewinding, Repeat, Shuffle, Sleeping.
Each state has its own UI behavior and animation.

### Player screen layout
- Top App Bar: back button, current playlist name, overflow menu
- Album Artwork: occupies ~60% of screen height
  - Artwork options: rounded corners, square, circular, full bleed, blurred background, rotating artwork toggle, parallax movement, dynamic color extraction, shared element transition from song list
- Song info display: title, artist, album, year, genre, audio format, bitrate, sample rate, bit depth (if available), track number, disc number, duration, file size, file path (advanced view)

### Playback controls
Previous, Rewind 10 seconds (optional), Play/Pause, Next, Seek bar, Elapsed time, Remaining time, Repeat, Shuffle, Like, Download (see note below), Share, Queue, Lyrics, Equalizer, Sleep timer, Playback speed, Overflow menu.

Buttons should provide: ripple animation, haptic feedback, smooth scaling animation, disabled state.

> **Note (gap filled):** In Phase 1, "Download" applies to imported/queued files being copied into managed local storage from external sources (e.g., USB/SD import). In Phase 2, once the catalog/streaming source exists, this same control becomes "download for offline playback" of a remote catalog track — same button, same `MediaItem` model, different backing repository. Don't build the button as local-only-hardcoded.

### Seek bar
Support: smooth dragging, tap to seek, preview timestamp, animated thumb, buffered progress, played progress, remaining progress, waveform mode (optional).

### Gestures
- Swipe down: minimize player
- Swipe left: next track
- Swipe right: previous track
- Double tap artwork: favourite
- Long press artwork: open quick actions
- Pinch artwork: toggle fullscreen artwork

### Queue
Queue screen supports: current song, upcoming songs, played songs history, drag-and-drop reorder, swipe to remove, play next, move to top, move to bottom, save queue as playlist, clear queue, repeat queue, shuffle queue, queue statistics. Queue persists after reboot.

### Playback modes
Normal, Repeat One, Repeat All, Shuffle, Shuffle Albums, Shuffle Artists, Smart Shuffle, Party Mode, Gapless Mode, Crossfade Mode.

**Crossfade:** user-adjustable duration (Off, 1s, 2s, 3s, 5s, 8s, 10s), smooth fade-in and fade-out.
**Gapless playback:** required — no silence between albums designed for continuous playback.

### Playback speed
Supported: 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x. Pitch preservation optional.

### Sleep timer
Options: 10 min, 15 min, 30 min, 45 min, 60 min, 90 min, 120 min, End of current track, End of playlist. Fade out during last 10 seconds.

### Equalizer
Dedicated screen. 10-band EQ.
**Bands:** 31 Hz, 62 Hz, 125 Hz, 250 Hz, 500 Hz, 1 kHz, 2 kHz, 4 kHz, 8 kHz, 16 kHz.
**Additional controls:** Bass Boost, Treble Boost, Virtualizer (if supported), Balance, Preamp, Limiter.

**EQ Presets:** Normal, Pop, Rock, Jazz, Hip Hop, Dance, Electronic, Classical, Acoustic, Podcast, Vocal, Bass Boost, Treble Boost, Custom.
Users can: save presets, rename presets, delete custom presets, import/export presets.

### Audio quality display
Show: MP3, AAC, FLAC, ALAC, WAV, OGG, Opus current quality — bitrate, sample rate, bit depth, channels, codec.

### Bluetooth
Automatically detect: device name, codec, battery level (if available), connection quality, supported codecs (SBC, AAC, aptX, aptX HD, aptX Adaptive, LDAC, LC3, LE Audio).
Show Bluetooth icon inside player. Allow switching between: phone speaker, Bluetooth device, wired headset, USB DAC — without stopping playback.

### Wired headphones
Detect insertion, detect removal.
User options: Pause on disconnect, Resume on reconnect, Ignore, Remember preference.

### USB DAC
Detect external DAC, display manufacturer/audio capabilities (sample rate, bit depth). Allow direct output if supported.

### Audio focus
Handle correctly for: incoming call, alarm, navigation voice, assistant, notification sounds, another music app.
User options: Pause, Duck volume, Ignore, Resume automatically.

### Notification
Persistent media notification.
**Contents:** artwork, song title, artist, Play/Pause, Previous, Next, Stop, Like (optional), Dismiss (optional).
Notification color extracted from artwork.

### Lock screen
**Display:** artwork, song, artist, playback controls, progress, dynamic color.
Support biometric unlock without interrupting playback.

### Widgets
Support sizes: 2x2, 4x2, 4x4, transparent, dynamic colors.
**Widget actions:** Play/Pause, Next, Previous, Favourite, Shuffle. Artwork updates automatically.

### Android Auto
Browse: Albums, Artists, Songs, Playlists, Folders, Search, voice commands, queue management, playback controls.

### Playback history
Store: date played, time played, duration listened, completion percentage, skip count, favourite status. Used for smart recommendations.

### Smart resume (after reboot)
Restore: queue, playback position, playback speed, EQ, repeat mode, shuffle state.

### Error handling
Gracefully handle: corrupt file, missing file, unsupported codec, permission revoked, storage removed, Bluetooth disconnected, decoder failure, database corruption.
Display clear, user-friendly messages and continue functioning without crashes.

### Performance goals
- Playback starts in under 150 ms for local files where possible
- Scrolling remains smooth at 60/90/120 Hz
- Album artwork loads asynchronously with caching
- No audio glitches during background operation
- Memory leaks prevented through lifecycle-aware components
- Battery usage minimized during idle and playback

---

## 9. Library Management, Playlists, Metadata, Database, Import System & Local Storage

### Library philosophy
The library is the user's personal music collection. It must feel fast, organized, and intelligent, whether the source is on-device storage (Phase 1) or a future remote catalog (Phase 2). The user should never feel lost, even with over 100,000 songs. Everything should load instantly using efficient Room queries, pagination, and background indexing.

### Library structure (sections)
All Songs, Albums, Artists, Genres, Composers, Playlists, Favourites, Recently Played, Recently Added, Most Played, Downloads, Hidden Songs, Hidden Albums, Hidden Artists, Folder View, SD Card, USB Storage, Imported Music.
Allow users to customize which sections appear and in what order.

### Songs screen
Each song row displays: album artwork, song title, artist, album, duration, quality badge (FLAC, MP3, AAC, etc.), favourite icon (optional), playing indicator, download/import status.
Tapping a song: starts playback, updates "Recently Played," increments play count.
Long-press: opens contextual action menu.

### Context menu actions
Play, Play Next, Add to Queue, Add to Playlist, Create Playlist, Favourite/Unfavourite, Edit Metadata, Rename Display Title, Change Album Art, File Information, Delete from Library (does not delete the actual file by default), Hide, Share, View Album, View Artist, Go to Folder, Delete File (requires confirmation).
Support multi-select mode.

### Multi-select mode actions
Add all to playlist, Queue all, Favourite all, Hide all, Share, Export playlist, Delete from library, Delete files, Select all, Invert selection.

### Albums
Album page shows: artwork, album title, artist, year, genre, track count, total duration, total size.
Actions: Play, Shuffle, Favourite, Share, Edit metadata.
Track list sorted by: disc number, track number.
Sort options: alphabetically, by duration, by date added.

### Artists
Artist screen includes: artist image (if available), biography placeholder (optional local notes), albums, singles, songs, total play count.
Quick actions: Play All, Shuffle, Favourite toggle, Hide artist.

### Genres
Display: genre artwork, number of albums, number of artists, number of tracks.
Genres support: Play All, Shuffle, Favourite.

### Folder Browser
Support: Internal Storage, SD Card, USB OTG.
Folders display: folder artwork, folder name, song count, total size.
Users can: favourite folders, scan specific folders, ignore folders, rename display name (library only).

### Playlists
Support unlimited playlists. Types: Manual, Smart, Temporary, Imported.
Each playlist contains: artwork (auto-generated collage), name, description, track count, total duration, last modified, favourite status.

**Playlist operations:** Create, Rename, Delete, Duplicate, Merge, Export, Import, Pin, Hide, Sort, Filter, Share, Generate artwork collage automatically.

### Smart Playlists (rule-based)
Most Played, Recently Added, Recently Played, Never Played, Favourite Songs, High Quality Audio, FLAC Collection, Longest Songs, Shortest Songs, Recently Imported, Songs Added This Week, Songs Added This Month, Songs Added This Year, Custom rule-based playlists.

### Favourites
Separate sections: Favourite Songs, Favourite Albums, Favourite Artists, Favourite Playlists, Favourite Folders. One tap to access.

### Hidden Collection
Hidden content does not appear in searches or normal browsing. Protected with optional: PIN, Pattern, Biometric authentication.
Users can hide: Songs, Albums, Artists, Playlists, Folders.

### Recently Played
Store: date, time, playback duration, completion %, skip count, playback source. History size configurable.
Options: Clear history, pause history recording, export history.

### Most Played
Ranking based on: play count, completion rate, listening time.
Time period filters: Today, Week, Month, Year, All Time.

### Metadata Editor
**Editable fields:** Title, Artist, Album, Album Artist, Genre, Composer, Track Number, Disc Number, Year, Comment, Lyrics, Artwork, Rating, Display Name.
Changes can be: app-only (stored in Room), or written to the file if supported and the user grants permission.

**Album artwork options:** Use embedded artwork, choose image from gallery, take photo, remove artwork, download from user source (future/manual import), crop tool, automatic color extraction after change.

### Lyrics
Support: embedded lyrics, local `.lrc` files, unsynchronized lyrics, synchronized lyrics.
Users can: edit, import, export, hide, adjust timing offset.

### Search filters
Filter by: Songs, Albums, Artists, Genres, Folders, Playlists, Composers, File Name, File Extension, Year, Duration, Bitrate, Sample Rate.
Supports: fuzzy search, prefix search, partial match, recent searches, saved searches.

### Import system
**First launch:** scan device automatically; user selects folders to include or exclude.
Manual rescan available at any time.
**Detect:** new files, deleted files, renamed files, moved files, metadata changes, duplicate tracks.

### Duplicate detection
Compare by: file path, file hash (optional), duration, metadata.
User chooses: Keep newest, Keep oldest, Keep highest quality, Keep all.

### Database (Room)
**Entities:** Song, Album, Artist, Genre, Playlist, PlaylistSong, Favourite, History, Hidden Item, Folder, Settings Override, Lyrics, Artwork, Equalizer Preset.
Use indexed columns for common queries (title, artist, album, genre, play count).

### Preferences (DataStore)
Store: Theme, Accent color, Playback speed, Repeat mode, Shuffle mode, Equalizer preset, Crossfade duration, Sleep timer defaults, Notification preferences, Gesture settings, Folder inclusion/exclusion, Sort preferences, Grid/List preference.

### Permissions (Android 13+)
Request only when needed: `READ_MEDIA_AUDIO`, `READ_MEDIA_IMAGES` (for custom artwork), `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.
Gracefully explain why permissions are needed and continue with reduced functionality if declined.

### Accessibility
Support: TalkBack, Large text, High contrast, Reduced motion, One-handed mode, Keyboard navigation (where applicable). Touch target size ≥ 48 dp.

### Edge cases to handle gracefully
SD card removed while playing, USB storage disconnected, corrupt metadata, missing album artwork, very long file names, emoji and multilingual metadata, empty library, duplicate songs, storage full, database migration after app updates.
The app should recover automatically whenever possible without crashing or losing user data.

---

## 10. Settings, System, Themes, Notifications, Widgets, Security, Performance, Testing & Production Readiness

### Settings architecture
Create a dedicated settings module. Settings must be: local-only, persistent, reactive, instantly applied, categorized clearly, searchable.
Use: DataStore Preferences for simple settings; Room for complex configurations.

### Settings Home (categories)
Playback Options, Audio Settings, Download/Storage Settings, Library Settings, Appearance Settings, Player Customisation, Gesture Controls, Notification Settings, Privacy Settings, Backup System, About Section, Help System.

#### Playback Options
Default playback mode, shuffle behavior, repeat behavior, resume playback, remember queue, auto-play after connecting device, auto-play after app launch, skip silence, crossfade, gapless playback, playback speed memory, volume normalization.

#### Audio Settings
Controls: Equalizer, Bass Boost, Virtualizer, Audio balance, Mono audio, High quality audio mode, Disable audio effects, External DAC mode, Bluetooth audio preferences, Wired headset behavior, Speaker optimization.

#### Download / Storage Settings
Display: Storage used, Music library size, Cache size, Artwork cache size, Database size, Available storage.
Controls: Clear cache, Clear artwork cache, Move downloads, Change scan folders, Automatic scanning, Scan schedule, Storage warning threshold.

#### Library Settings
Options: Default library tab, Default sorting (Alphabetical/Recently added/Recently played/Most played/Custom), View style (List/Grid/Compact list), Show: Album artist, File name, Folder name, Quality badge, Duration.

#### Appearance Settings
Theme (Light/Dark/AMOLED Black/System default), Accent (Dynamic color/Custom color), Animations (Full/Reduced/No animations), Artwork (Blur background/Animated artwork/Rotating artwork/Motion effects).

#### Player Customisation
Allow users to customize: Player layout, Button positions, Visible controls, Artwork shape, Background style, Lyrics style, Seek bar style, Mini player style, Gesture controls.

#### Gesture Controls
Configurable actions: Double tap artwork, Play/pause, Next song, Previous song, Disable, Shake device, Next song, Random song, Disable, Long press → Open menu, Equalizer, Lyrics.

#### Notification Settings
Enable media notification, Show artwork, Show like button, Show next button, Show previous button, Show progress, Compact notification, Lock screen visibility.

#### Privacy Settings
Since there is no cloud account, privacy features are local.
Options: App lock, Biometric protection, PIN protection, Hide private playlists, Hide private folders, Disable listening history, Clear listening history, Clear search history, Export local data, Import backup.

#### Backup System
Create local backup files.
**Backup includes:** Playlists, Favourites, History, Settings, Equalizer presets, Metadata changes, Hidden items.
**Does NOT include:** Music files themselves (because they may be very large).
Support: Export backup, Import backup, Automatic backup reminders.

#### About Section
Display: Application name, Version, Build number, Open-source licenses, Developer information, Privacy policy (local document), Terms of use, Changelog, Contact option.

#### Help System
Include: Getting started guide, Audio format explanation, Permission explanation, FAQ, Troubleshooting, Storage guide, Bluetooth guide.

### Home Screen Widgets (via Glance)
- **Small widget:** artwork, song title, Play/Pause, Next
- **Medium widget:** artwork, song, artist, Previous, Play, Next, Favourite
- **Large widget:** artwork, queue preview, playback controls, playlist shortcut

### Quick Settings Tile
Create Android Quick Settings tile.
Actions: Play/Pause, Next track, Open app, Shuffle.

### App Shortcuts (long press app icon)
Shortcuts: Play favourite songs, Resume playback, Open library, Search music, Open playlists.

### Share Integration
Support Android share sheet.
Users can share: Songs, Albums, Artists, Playlists, Metadata, Text information.
Generate: Plain text share, QR code share (local playlist transfer).

### Local Device Transfer
Allow transferring playlists between devices without accounts.
**Methods:** Export file, Import file, Nearby Share integration, QR transfer, Bluetooth transfer (optional).

### Security architecture
Implement: Encrypted DataStore where needed, Encrypted Room database for sensitive data, BiometricPrompt API, no unnecessary internet permissions, no hidden tracking, no user account requirement.

### App permissions policy
Request minimum permissions.
**Never request:** Contacts, Location, Camera, Microphone — unless a future feature explicitly requires it.
Explain every permission.

### Performance architecture

**Startup optimization** — Target: cold start under 1.5 seconds.
Techniques: Lazy initialization, Dependency injection optimization, Database preloading, Splash screen optimization.

**Memory management**
Prevent: Memory leaks, large artwork memory usage, unreleased player objects, background service leaks.
Use: Lifecycle-aware components, image resizing, bitmap caching limits.

**Battery optimization**
Reduce: Background scans, database writes, artwork processing, unnecessary animations.
Use: WorkManager constraints, charging-only heavy tasks.

**Large library optimization**
Must support: 10 songs, 1,000 songs, 10,000 songs, 100,000+ songs.
Techniques: Paging 3, database indexing, lazy lists, incremental scanning, batch inserts, background processing.

### Error handling system
Every feature requires: loading state, success state, empty state, error state, retry option.
**Examples:** Empty library → "No music found. Scan your device to begin."; Permission denied → "Music access is required to display your library."; Storage unavailable → "Storage device disconnected."

### Logging
Implement structured logging.
- Development: detailed logs
- Production: minimal logs — never log private file paths unnecessarily, user data, or personal information

### Analytics (optional, local-only)
No cloud analytics. Store locally: most played genres, usage time, feature usage, playback statistics.
Allow user to: Clear analytics, Disable analytics.

### Testing strategy

**Unit testing:** Repositories, Use cases, Database operations, Playback logic, Playlist logic, Search algorithms.

**UI testing:** Navigation, Player controls, Library screens, Settings, Dialogs, Themes.

**Integration testing:** Media scanner, Database migrations, Import/export, Backup restore, Bluetooth events.

### Release preparation
Before publishing, check: App size, Startup time, Battery usage, Memory usage, Crash rate, Accessibility, Permissions, Android version compatibility.

### Supported devices
Optimize for: Budget Android phones, Mid-range phones, Flagship phones, Tablets, Foldables, High refresh rate screens, Large storage devices.

### Android versions
Minimum: Android 13. Recommended: Android 14+. Optimized: Android 15/16.

### Production checklist (before release)
- [ ] No crashes during normal use
- [ ] Music continues when screen locks
- [ ] Queue survives restart
- [ ] Database migrations tested
- [ ] Permissions handled correctly
- [ ] Backup/restore works
- [ ] Dark mode complete
- [ ] Accessibility verified
- [ ] Battery usage acceptable
- [ ] Large libraries tested
- [ ] Bluetooth tested
- [ ] Wired audio tested
- [ ] Notifications tested
- [ ] Widgets tested
- [ ] Android Auto tested
- [ ] **(added)** Onboarding / permission-request flow tested end-to-end
- [ ] **(added)** App survives interrupted scans (killed mid-scan, storage removed mid-scan)
- [ ] **(added)** Database migration path tested across at least 2 prior schema versions
- [ ] **(added)** Cold start measured on a low-end device, not just flagship
- [ ] **(added)** Rotation/foldable unfold-fold tested mid-playback
- [ ] **(added)** Language/locale switching tested (metadata with non-Latin scripts)

---

## 11. Gaps Filled / Additions Not in Original Notes

These were implied by the spec but not explicitly spelled out — added for completeness so an AI coding agent doesn't have to guess:

1. **Onboarding flow:** First-launch sequence — welcome screen → permission requests (with rationale) → initial folder selection → first scan progress screen → Home.
2. **First-run empty states:** Explicit copy and CTA for zero-library state (see Section 4).
3. **App versioning & DB migration strategy:** Room migrations must be explicit and tested; never rely on destructive fallback in production builds.
4. **CI/CD guidance:** Recommend GitHub Actions for lint, unit tests, and Compose UI test runs on PRs; gate merges on green checks.
5. **Crash reporting (local-only):** Since there's no cloud account, consider an opt-in local crash log export the user can manually attach to a bug report — no automatic remote crash reporting given the "no hidden tracking" principle.
6. **Localization:** App strings should be externalized from day one (even if only English ships initially) to avoid costly retrofits.
7. **Tablet/foldable layout behavior:** Explicit two-pane layout for Library + Player on large screens (not just "designed for tablets" — specify master-detail pattern).
8. **Download vs. Import terminology:** Clarified in Section 8 — Phase 1 "Download" means local import/copy from external storage; Phase 2 reuses the same control for offline-caching a streamed catalog track. Same UI, same `MediaItem`, different repository underneath.
9. **Data export format:** Backup/playlist export files should use a documented, versioned JSON schema so imports remain forward-compatible across app versions.
10. **Notification channel setup:** Android 13+ requires explicit notification channel creation before `POST_NOTIFICATIONS` prompt — call this out as its own implementation step.

---

## 12. Completeness Checklist — Screens, Gestures, States

The original spec is thorough on Player/Library/Settings, but a few things an agent needs explicitly called out were missing or implicit. Adding them here rather than scattering edits everywhere:

### Screens not fully specified above
- **Splash/launch screen** — branded splash using Android 12+ SplashScreen API (icon + background color, not a custom layout hack)
- **Onboarding sequence** (3–4 screens): Welcome → permission rationale → folder selection → first-scan progress (referenced in Section 11 item 1, but build as real screens, not a single dialog)
- **Landscape player layout** — the ~60%-artwork player layout in Section 8 is portrait-only; specify a landscape variant (artwork left, controls/queue right) for phones and tablets
- **Tablet/foldable two-pane layout** — master-detail: library list pane + persistent now-playing/detail pane side by side (referenced in Section 11 item 7 — make sure it's an actual Compose adaptive layout, e.g. via `WindowSizeClass`)
- **"Now Playing" full lyrics screen** — a dedicated full-screen lyrics view (swipe up from player), not just an inline lyrics panel
- **Artist/Album "See all" grid screens** — when a Home/Discover section is tapped, a full grid/list screen, not just horizontal scroll
- **File info / advanced metadata screen** — the "advanced view" mentioned in Section 8 needs its own screen, not just a modal row
- **Storage permission rationale screen** — distinct from the general onboarding permission screen; shown again if permission is revoked later
- **Backup/restore progress screen** — with progress state, not just a settings toggle
- **"What's new" / changelog screen** — referenced in About, but should be an actual screen, shown once per version update
- **Add-to-playlist bottom sheet** — a specific reusable sheet (search existing playlists + "create new" inline), distinct from the general playlist screens

### Gestures not fully specified above
- **Pull-to-refresh** on Library/Home to trigger manual rescan
- **Swipe-to-delete / swipe-to-remove** on list rows (playlists, queue, search history) — direction and reveal-action pattern should be consistent app-wide
- **Long-press drag reorder** on playlist tracks (distinct from queue reorder already specified)
- **Pinch-to-zoom** on grid views (album/artist grid density adjustment) — optional but common in ColorOS-style galleries
- **Edge swipe back** — respect Android predictive back gesture (Android 14+) system-wide, including custom transitions on the player and full-screen lyrics screens
- **Long-press app shortcut previews** — already covered for shortcuts list, but confirm dynamic shortcuts update as favorites change
- **Drag-to-reveal mini player → full player** — explicit vertical drag threshold and velocity-based fling behavior, not just tap-to-expand

### States not fully specified above
- **Offline/no-storage-permission state** vs. **empty library state** — these need distinct copy and illustration (permission-denied is recoverable via Settings deep link; empty library needs a Scan CTA)
- **RTL layout support** — mirror all layouts for right-to-left locales (Arabic, Hebrew, Urdu) if any non-English locale ships
- **Low storage warning state** — proactive banner when device storage is critically low (distinct from the "storage full" edge case in Section 9)
- **First-scan large-library progress state** — a real progress screen/notification with count and estimated time for very large libraries (10,000+ tracks), not just a spinner

---

## 13. Phase 2 Readiness — Catalog & Streaming (build later, design for now)

This section is **not** part of the Phase 1 build task. It exists so the AI coding agent building Phase 1 doesn't make decisions that block it.

**What Phase 2 will add:**
- A remote music catalog fetch (browse/search a hosted library, not just the device's files)
- Streaming playback of catalog tracks alongside local files
- Mixed queues (local + streamed tracks back to back)
- Optional offline caching of streamed tracks (reusing the existing Download UI — see item 8 above)

**Hard constraint carried into Phase 2: no user accounts, no login, no OAuth, no cloud auth of any kind.** Whatever catalog/streaming source is chosen later must work with either no key at all or a static app-level API key baked into the client — never a per-user identity. If this becomes technically impossible with a given provider, that's a signal to pick a different provider, not to add login.

**What Phase 1 must NOT do**, so Phase 2 stays additive:
- Do not name local-only concepts things like `LocalSong` if a shared `MediaItem` is more correct — see Section 2a.
- Do not write UI (Home, Discover, Search, Player, Queue) against concrete Room types — go through the repository interface.
- Do not assume `PlayerManager` only ever receives a `file://` URI — resolve URIs through an abstraction so `https://` stream URLs slot in later.
- Do not skip building the `network/` module scaffold now — an empty Retrofit/OkHttp setup (even unused) is cheaper than retrofitting one later.
- Do keep the actual API/provider choice for Phase 2 **out of scope** for this prompt — that decision, and its endpoint contracts, will be specified separately when Phase 2 begins.

---

*End of master specification. This document consolidates and reorganizes the full four-part prompt (Core/Design, Audio Engine, Library/Metadata, Settings/Production), plus a screens/gestures/states completeness pass checked against current ColorOS design language, into one continuous reference an AI coding agent can use as the primary blueprint for building the Phase 1 app — architected so the Phase 2 catalog/streaming layer (no accounts, ever) can be added without a rewrite.*
