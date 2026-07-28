package com.aurora.music.domain.repository

import com.aurora.music.domain.model.AudioFocusBehaviour
import com.aurora.music.domain.model.CrossfadeDuration
import com.aurora.music.domain.model.EqualizerSettings
import com.aurora.music.domain.model.HeadsetDisconnectBehaviour
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.RepeatMode
import com.aurora.music.domain.model.ShuffleMode
import kotlinx.coroutines.flow.Flow

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

enum class AccentSource { DYNAMIC, ARTWORK, CUSTOM }

enum class AnimationLevel { FULL, REDUCED, NONE }

enum class LibraryViewStyle { LIST, GRID, COMPACT }

enum class SortOrder { TITLE, ARTIST, ALBUM, DATE_ADDED, DATE_PLAYED, PLAY_COUNT, DURATION }

enum class ArtworkShape { ROUNDED, SQUARE, CIRCLE, FULL_BLEED }

data class AppSettings(
    // Appearance
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentSource: AccentSource = AccentSource.DYNAMIC,
    val customAccentColor: Long = 0xFF7C6BFFL,
    val animationLevel: AnimationLevel = AnimationLevel.FULL,
    val contourGlowEnabled: Boolean = true,
    val blurredPlayerBackground: Boolean = true,
    val rotatingArtwork: Boolean = false,
    val parallaxArtwork: Boolean = true,
    val artworkShape: ArtworkShape = ArtworkShape.ROUNDED,

    // Playback
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val playbackSpeed: PlaybackSpeed = PlaybackSpeed.X1,
    val rememberQueue: Boolean = true,
    val resumeOnLaunch: Boolean = false,
    val gaplessPlayback: Boolean = true,
    val crossfade: CrossfadeDuration = CrossfadeDuration.OFF,
    val skipSilence: Boolean = false,
    val volumeNormalization: Boolean = false,
    val audioFocusBehaviour: AudioFocusBehaviour = AudioFocusBehaviour.PAUSE,
    val headsetBehaviour: HeadsetDisconnectBehaviour = HeadsetDisconnectBehaviour.PAUSE,

    // Library
    val defaultLibraryTab: String = "songs",
    val sortOrder: SortOrder = SortOrder.TITLE,
    val viewStyle: LibraryViewStyle = LibraryViewStyle.LIST,
    val showQualityBadge: Boolean = true,
    val showDuration: Boolean = true,
    val excludedFolders: Set<String> = emptySet(),
    val includedFolders: Set<String> = emptySet(),
    val automaticScanning: Boolean = true,
    val minTrackDurationSeconds: Int = 20,

    // Notifications
    val showNotificationArtwork: Boolean = true,
    val showLikeInNotification: Boolean = true,
    val compactNotification: Boolean = false,

    // Privacy
    val appLockEnabled: Boolean = false,
    val historyRecordingEnabled: Boolean = true,
    val localAnalyticsEnabled: Boolean = true,

    // Onboarding
    val onboardingComplete: Boolean = false,
    val lastSeenVersionCode: Int = 0,
)

interface SettingsRepository {
    val settings: Flow<AppSettings>
    val equalizer: Flow<EqualizerSettings>

    suspend fun update(transform: (AppSettings) -> AppSettings)
    suspend fun updateEqualizer(transform: (EqualizerSettings) -> EqualizerSettings)
    suspend fun current(): AppSettings
}
