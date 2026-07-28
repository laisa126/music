package com.aurora.music.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.data.mediastore.MediaStoreScanner
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.ScanState
import com.aurora.music.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val scanner: MediaStoreScanner,
) : ViewModel() {

    val scanState: StateFlow<ScanState> = repository.scanState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScanState.Idle)

    private val _hasPermission = MutableStateFlow(scanner.hasAudioPermission())
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    fun refreshPermission() {
        _hasPermission.value = scanner.hasAudioPermission()
    }

    fun startScan() {
        viewModelScope.launch { repository.rescan(force = true) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.update { it.copy(onboardingComplete = true) } }
    }
}
