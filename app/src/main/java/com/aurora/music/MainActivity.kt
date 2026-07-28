package com.aurora.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aurora.music.core.designsystem.theme.AuroraTheme
import com.aurora.music.data.scanner.LibraryScanWorker
import com.aurora.music.domain.repository.AppSettings
import com.aurora.music.domain.repository.SettingsRepository
import com.aurora.music.player.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val playerManager: PlayerManager,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        playerManager.initialize()
    }

    override fun onCleared() {
        // The service keeps playing; only the UI-side controller goes away.
        super.onCleared()
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the splash until preferences are loaded so we never flash the
        // wrong theme or bounce the user into onboarding they've already done.
        var ready = false
        splash.setKeepOnScreenCondition { !ready }

        lifecycleScope.launch {
            val settings = settingsRepository.settings.first()
            ready = true
            if (settings.automaticScanning && settings.onboardingComplete) {
                LibraryScanWorker.runNow(this@MainActivity)
                LibraryScanWorker.schedulePeriodic(this@MainActivity)
            }
        }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val resolved = settings ?: AppSettings()

            AuroraTheme(settings = resolved) {
                com.aurora.music.navigation.AuroraApp(
                    onboardingComplete = resolved.onboardingComplete,
                )
            }
        }
    }
}
