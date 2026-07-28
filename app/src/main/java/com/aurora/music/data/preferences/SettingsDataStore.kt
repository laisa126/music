package com.aurora.music.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aurora.music.domain.model.AudioFocusBehaviour
import com.aurora.music.domain.model.CrossfadeDuration
import com.aurora.music.domain.model.EQ_BANDS_HZ
import com.aurora.music.domain.model.EqualizerSettings
import com.aurora.music.domain.model.HeadsetDisconnectBehaviour
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.RepeatMode
import com.aurora.music.domain.model.ShuffleMode
import com.aurora.music.domain.repository.AccentSource
import com.aurora.music.domain.repository.AnimationLevel
import com.aurora.music.domain.repository.AppSettings
import com.aurora.music.domain.repository.ArtworkShape
import com.aurora.music.domain.repository.LibraryViewStyle
import com.aurora.music.domain.repository.SettingsRepository
import com.aurora.music.domain.repository.SortOrder
import com.aurora.music.domain.repository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aurora_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val store = context.dataStore

    private val preferences: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    override val settings: Flow<AppSettings> = preferences.map { it.toAppSettings() }

    override val equalizer: Flow<EqualizerSettings> = preferences.map { it.toEqualizerSettings() }

    override suspend fun current(): AppSettings = settings.first()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { prefs ->
            val updated = transform(prefs.toAppSettings())
            prefs.writeAppSettings(updated)
        }
    }

    override suspend fun updateEqualizer(transform: (EqualizerSettings) -> EqualizerSettings) {
        store.edit { prefs ->
            val updated = transform(prefs.toEqualizerSettings())
            prefs.writeEqualizerSettings(updated)
        }
    }

    private fun Preferences.toAppSettings() = AppSettings(
        themeMode = enumOf(this[Keys.THEME_MODE], ThemeMode.SYSTEM),
        accentSource = enumOf(this[Keys.ACCENT_SOURCE], AccentSource.DYNAMIC),
        customAccentColor = this[Keys.CUSTOM_ACCENT] ?: 0xFF7C6BFFL,
        animationLevel = enumOf(this[Keys.ANIMATION_LEVEL], AnimationLevel.FULL),
        contourGlowEnabled = this[Keys.CONTOUR_GLOW] ?: true,
        blurredPlayerBackground = this[Keys.BLUR_BACKGROUND] ?: true,
        rotatingArtwork = this[Keys.ROTATING_ARTWORK] ?: false,
        parallaxArtwork = this[Keys.PARALLAX_ARTWORK] ?: true,
        artworkShape = enumOf(this[Keys.ARTWORK_SHAPE], ArtworkShape.ROUNDED),
        repeatMode = enumOf(this[Keys.REPEAT_MODE], RepeatMode.OFF),
        shuffleMode = enumOf(this[Keys.SHUFFLE_MODE], ShuffleMode.OFF),
        playbackSpeed = PlaybackSpeed.nearest(this[Keys.PLAYBACK_SPEED] ?: 1f),
        rememberQueue = this[Keys.REMEMBER_QUEUE] ?: true,
        resumeOnLaunch = this[Keys.RESUME_ON_LAUNCH] ?: false,
        gaplessPlayback = this[Keys.GAPLESS] ?: true,
        crossfade = CrossfadeDuration.fromSeconds(this[Keys.CROSSFADE_SECONDS] ?: 0),
        skipSilence = this[Keys.SKIP_SILENCE] ?: false,
        volumeNormalization = this[Keys.VOLUME_NORMALIZATION] ?: false,
        audioFocusBehaviour = enumOf(this[Keys.AUDIO_FOCUS], AudioFocusBehaviour.PAUSE),
        headsetBehaviour = enumOf(this[Keys.HEADSET_BEHAVIOUR], HeadsetDisconnectBehaviour.PAUSE),
        defaultLibraryTab = this[Keys.DEFAULT_LIBRARY_TAB] ?: "songs",
        sortOrder = enumOf(this[Keys.SORT_ORDER], SortOrder.TITLE),
        viewStyle = enumOf(this[Keys.VIEW_STYLE], LibraryViewStyle.LIST),
        showQualityBadge = this[Keys.SHOW_QUALITY_BADGE] ?: true,
        showDuration = this[Keys.SHOW_DURATION] ?: true,
        excludedFolders = this[Keys.EXCLUDED_FOLDERS] ?: emptySet(),
        includedFolders = this[Keys.INCLUDED_FOLDERS] ?: emptySet(),
        automaticScanning = this[Keys.AUTOMATIC_SCANNING] ?: true,
        minTrackDurationSeconds = this[Keys.MIN_TRACK_DURATION] ?: 20,
        showNotificationArtwork = this[Keys.NOTIFICATION_ARTWORK] ?: true,
        showLikeInNotification = this[Keys.NOTIFICATION_LIKE] ?: true,
        compactNotification = this[Keys.NOTIFICATION_COMPACT] ?: false,
        appLockEnabled = this[Keys.APP_LOCK] ?: false,
        historyRecordingEnabled = this[Keys.HISTORY_ENABLED] ?: true,
        localAnalyticsEnabled = this[Keys.ANALYTICS_ENABLED] ?: true,
        onboardingComplete = this[Keys.ONBOARDING_COMPLETE] ?: false,
        lastSeenVersionCode = this[Keys.LAST_SEEN_VERSION] ?: 0,
    )

    private fun MutablePreferencesScope.writeAppSettings(s: AppSettings) {
        this[Keys.THEME_MODE] = s.themeMode.name
        this[Keys.ACCENT_SOURCE] = s.accentSource.name
        this[Keys.CUSTOM_ACCENT] = s.customAccentColor
        this[Keys.ANIMATION_LEVEL] = s.animationLevel.name
        this[Keys.CONTOUR_GLOW] = s.contourGlowEnabled
        this[Keys.BLUR_BACKGROUND] = s.blurredPlayerBackground
        this[Keys.ROTATING_ARTWORK] = s.rotatingArtwork
        this[Keys.PARALLAX_ARTWORK] = s.parallaxArtwork
        this[Keys.ARTWORK_SHAPE] = s.artworkShape.name
        this[Keys.REPEAT_MODE] = s.repeatMode.name
        this[Keys.SHUFFLE_MODE] = s.shuffleMode.name
        this[Keys.PLAYBACK_SPEED] = s.playbackSpeed.value
        this[Keys.REMEMBER_QUEUE] = s.rememberQueue
        this[Keys.RESUME_ON_LAUNCH] = s.resumeOnLaunch
        this[Keys.GAPLESS] = s.gaplessPlayback
        this[Keys.CROSSFADE_SECONDS] = s.crossfade.seconds
        this[Keys.SKIP_SILENCE] = s.skipSilence
        this[Keys.VOLUME_NORMALIZATION] = s.volumeNormalization
        this[Keys.AUDIO_FOCUS] = s.audioFocusBehaviour.name
        this[Keys.HEADSET_BEHAVIOUR] = s.headsetBehaviour.name
        this[Keys.DEFAULT_LIBRARY_TAB] = s.defaultLibraryTab
        this[Keys.SORT_ORDER] = s.sortOrder.name
        this[Keys.VIEW_STYLE] = s.viewStyle.name
        this[Keys.SHOW_QUALITY_BADGE] = s.showQualityBadge
        this[Keys.SHOW_DURATION] = s.showDuration
        this[Keys.EXCLUDED_FOLDERS] = s.excludedFolders
        this[Keys.INCLUDED_FOLDERS] = s.includedFolders
        this[Keys.AUTOMATIC_SCANNING] = s.automaticScanning
        this[Keys.MIN_TRACK_DURATION] = s.minTrackDurationSeconds
        this[Keys.NOTIFICATION_ARTWORK] = s.showNotificationArtwork
        this[Keys.NOTIFICATION_LIKE] = s.showLikeInNotification
        this[Keys.NOTIFICATION_COMPACT] = s.compactNotification
        this[Keys.APP_LOCK] = s.appLockEnabled
        this[Keys.HISTORY_ENABLED] = s.historyRecordingEnabled
        this[Keys.ANALYTICS_ENABLED] = s.localAnalyticsEnabled
        this[Keys.ONBOARDING_COMPLETE] = s.onboardingComplete
        this[Keys.LAST_SEEN_VERSION] = s.lastSeenVersionCode
    }

    private fun Preferences.toEqualizerSettings(): EqualizerSettings {
        val raw = this[Keys.EQ_GAINS]
        val gains = raw?.split(',')?.mapNotNull { it.toFloatOrNull() }
            ?.takeIf { it.size == EQ_BANDS_HZ.size }
            ?: List(EQ_BANDS_HZ.size) { 0f }
        return EqualizerSettings(
            enabled = this[Keys.EQ_ENABLED] ?: false,
            presetName = this[Keys.EQ_PRESET] ?: "Normal",
            gains = gains,
            bassBoost = this[Keys.EQ_BASS] ?: 0f,
            trebleBoost = this[Keys.EQ_TREBLE] ?: 0f,
            virtualizer = this[Keys.EQ_VIRTUALIZER] ?: 0f,
            balance = this[Keys.EQ_BALANCE] ?: 0f,
            preampDb = this[Keys.EQ_PREAMP] ?: 0f,
            limiterEnabled = this[Keys.EQ_LIMITER] ?: true,
        )
    }

    private fun MutablePreferencesScope.writeEqualizerSettings(s: EqualizerSettings) {
        this[Keys.EQ_ENABLED] = s.enabled
        this[Keys.EQ_PRESET] = s.presetName
        this[Keys.EQ_GAINS] = s.gains.joinToString(",")
        this[Keys.EQ_BASS] = s.bassBoost
        this[Keys.EQ_TREBLE] = s.trebleBoost
        this[Keys.EQ_VIRTUALIZER] = s.virtualizer
        this[Keys.EQ_BALANCE] = s.balance
        this[Keys.EQ_PREAMP] = s.preampDb
        this[Keys.EQ_LIMITER] = s.limiterEnabled
    }

    private inline fun <reified T : Enum<T>> enumOf(name: String?, fallback: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_SOURCE = stringPreferencesKey("accent_source")
        val CUSTOM_ACCENT = longPreferencesKey("custom_accent")
        val ANIMATION_LEVEL = stringPreferencesKey("animation_level")
        val CONTOUR_GLOW = booleanPreferencesKey("contour_glow")
        val BLUR_BACKGROUND = booleanPreferencesKey("blur_background")
        val ROTATING_ARTWORK = booleanPreferencesKey("rotating_artwork")
        val PARALLAX_ARTWORK = booleanPreferencesKey("parallax_artwork")
        val ARTWORK_SHAPE = stringPreferencesKey("artwork_shape")

        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = stringPreferencesKey("shuffle_mode")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val REMEMBER_QUEUE = booleanPreferencesKey("remember_queue")
        val RESUME_ON_LAUNCH = booleanPreferencesKey("resume_on_launch")
        val GAPLESS = booleanPreferencesKey("gapless")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val VOLUME_NORMALIZATION = booleanPreferencesKey("volume_normalization")
        val AUDIO_FOCUS = stringPreferencesKey("audio_focus")
        val HEADSET_BEHAVIOUR = stringPreferencesKey("headset_behaviour")

        val DEFAULT_LIBRARY_TAB = stringPreferencesKey("default_library_tab")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val VIEW_STYLE = stringPreferencesKey("view_style")
        val SHOW_QUALITY_BADGE = booleanPreferencesKey("show_quality_badge")
        val SHOW_DURATION = booleanPreferencesKey("show_duration")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val INCLUDED_FOLDERS = stringSetPreferencesKey("included_folders")
        val AUTOMATIC_SCANNING = booleanPreferencesKey("automatic_scanning")
        val MIN_TRACK_DURATION = intPreferencesKey("min_track_duration")

        val NOTIFICATION_ARTWORK = booleanPreferencesKey("notification_artwork")
        val NOTIFICATION_LIKE = booleanPreferencesKey("notification_like")
        val NOTIFICATION_COMPACT = booleanPreferencesKey("notification_compact")

        val APP_LOCK = booleanPreferencesKey("app_lock")
        val HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")

        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val LAST_SEEN_VERSION = intPreferencesKey("last_seen_version")

        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET = stringPreferencesKey("eq_preset")
        val EQ_GAINS = stringPreferencesKey("eq_gains")
        val EQ_BASS = floatPreferencesKey("eq_bass")
        val EQ_TREBLE = floatPreferencesKey("eq_treble")
        val EQ_VIRTUALIZER = floatPreferencesKey("eq_virtualizer")
        val EQ_BALANCE = floatPreferencesKey("eq_balance")
        val EQ_PREAMP = floatPreferencesKey("eq_preamp")
        val EQ_LIMITER = booleanPreferencesKey("eq_limiter")
    }
}

private typealias MutablePreferencesScope = androidx.datastore.preferences.core.MutablePreferences
