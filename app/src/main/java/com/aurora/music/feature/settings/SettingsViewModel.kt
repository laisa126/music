package com.aurora.music.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.data.scanner.LibraryScanWorker
import com.aurora.music.domain.model.AudioFocusBehaviour
import com.aurora.music.domain.model.CrossfadeDuration
import com.aurora.music.domain.model.EqualizerPreset
import com.aurora.music.domain.model.EqualizerSettings
import com.aurora.music.domain.model.HeadsetDisconnectBehaviour
import com.aurora.music.domain.repository.AccentSource
import com.aurora.music.domain.repository.AnimationLevel
import com.aurora.music.domain.repository.AppSettings
import com.aurora.music.domain.repository.ArtworkShape
import com.aurora.music.domain.repository.LibraryViewStyle
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.SettingsRepository
import com.aurora.music.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val equalizer: StateFlow<EqualizerSettings> = settingsRepository.equalizer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EqualizerSettings())

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    private fun updateEq(transform: (EqualizerSettings) -> EqualizerSettings) {
        viewModelScope.launch { settingsRepository.updateEqualizer(transform) }
    }

    // ---- Appearance -------------------------------------------------------
    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setAccentSource(source: AccentSource) = update { it.copy(accentSource = source) }
    fun setCustomAccent(color: Long) = update { it.copy(customAccentColor = color) }
    fun setAnimationLevel(level: AnimationLevel) = update { it.copy(animationLevel = level) }
    fun setContourGlow(enabled: Boolean) = update { it.copy(contourGlowEnabled = enabled) }
    fun setBlurredBackground(enabled: Boolean) = update { it.copy(blurredPlayerBackground = enabled) }
    fun setRotatingArtwork(enabled: Boolean) = update { it.copy(rotatingArtwork = enabled) }
    fun setParallaxArtwork(enabled: Boolean) = update { it.copy(parallaxArtwork = enabled) }
    fun setArtworkShape(shape: ArtworkShape) = update { it.copy(artworkShape = shape) }

    // ---- Playback ---------------------------------------------------------
    fun setGapless(enabled: Boolean) = update { it.copy(gaplessPlayback = enabled) }
    fun setCrossfade(duration: CrossfadeDuration) = update { it.copy(crossfade = duration) }
    fun setSkipSilence(enabled: Boolean) = update { it.copy(skipSilence = enabled) }
    fun setVolumeNormalization(enabled: Boolean) = update { it.copy(volumeNormalization = enabled) }
    fun setRememberQueue(enabled: Boolean) = update { it.copy(rememberQueue = enabled) }
    fun setResumeOnLaunch(enabled: Boolean) = update { it.copy(resumeOnLaunch = enabled) }
    fun setAudioFocusBehaviour(b: AudioFocusBehaviour) = update { it.copy(audioFocusBehaviour = b) }
    fun setHeadsetBehaviour(b: HeadsetDisconnectBehaviour) = update { it.copy(headsetBehaviour = b) }

    // ---- Library ----------------------------------------------------------
    fun setViewStyle(style: LibraryViewStyle) = update { it.copy(viewStyle = style) }
    fun setShowQualityBadge(enabled: Boolean) = update { it.copy(showQualityBadge = enabled) }
    fun setShowDuration(enabled: Boolean) = update { it.copy(showDuration = enabled) }
    fun setMinTrackDuration(seconds: Int) = update { it.copy(minTrackDurationSeconds = seconds) }

    fun setAutomaticScanning(enabled: Boolean) {
        update { it.copy(automaticScanning = enabled) }
        if (enabled) {
            LibraryScanWorker.schedulePeriodic(context)
        } else {
            LibraryScanWorker.cancelPeriodic(context)
        }
    }

    fun excludeFolder(path: String) =
        update { it.copy(excludedFolders = it.excludedFolders + path) }

    fun includeFolder(path: String) =
        update { it.copy(excludedFolders = it.excludedFolders - path) }

    fun rescan() {
        viewModelScope.launch { musicRepository.rescan(force = true) }
    }

    // ---- Notifications ----------------------------------------------------
    fun setNotificationArtwork(enabled: Boolean) =
        update { it.copy(showNotificationArtwork = enabled) }

    fun setNotificationLike(enabled: Boolean) = update { it.copy(showLikeInNotification = enabled) }
    fun setCompactNotification(enabled: Boolean) = update { it.copy(compactNotification = enabled) }

    // ---- Privacy ----------------------------------------------------------
    fun setAppLock(enabled: Boolean) = update { it.copy(appLockEnabled = enabled) }
    fun setHistoryRecording(enabled: Boolean) = update { it.copy(historyRecordingEnabled = enabled) }
    fun setAnalytics(enabled: Boolean) = update { it.copy(localAnalyticsEnabled = enabled) }

    fun clearHistory() {
        viewModelScope.launch { musicRepository.clearHistory() }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { musicRepository.clearRecentSearches() }
    }

    // ---- Onboarding -------------------------------------------------------
    fun completeOnboarding() = update { it.copy(onboardingComplete = true) }

    // ---- Equalizer --------------------------------------------------------
    fun setEqualizerEnabled(enabled: Boolean) = updateEq { it.copy(enabled = enabled) }

    fun applyPreset(preset: EqualizerPreset) =
        updateEq { it.copy(presetName = preset.name, gains = preset.gains) }

    fun setBandGain(index: Int, gain: Float) = updateEq { current ->
        val gains = current.gains.toMutableList()
        if (index in gains.indices) gains[index] = gain.coerceIn(-12f, 12f)
        current.copy(gains = gains, presetName = "Custom")
    }

    fun setBassBoost(value: Float) = updateEq { it.copy(bassBoost = value) }
    fun setTrebleBoost(value: Float) = updateEq { it.copy(trebleBoost = value) }
    fun setVirtualizer(value: Float) = updateEq { it.copy(virtualizer = value) }
    fun setBalance(value: Float) = updateEq { it.copy(balance = value) }
    fun setPreamp(value: Float) = updateEq { it.copy(preampDb = value) }
    fun setLimiter(enabled: Boolean) = updateEq { it.copy(limiterEnabled = enabled) }
}
