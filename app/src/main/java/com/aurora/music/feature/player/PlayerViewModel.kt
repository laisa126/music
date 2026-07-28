package com.aurora.music.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.domain.model.Lyrics
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.PlayerUiState
import com.aurora.music.domain.model.SleepTimerOption
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.SettingsRepository
import com.aurora.music.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared across the mini player, full player, queue and lyrics screens so they
 * animate off one state object (Section 3 "parallel animations").
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val repository: MusicRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<PlayerUiState> = playerManager.state

    val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = com.aurora.music.domain.repository.AppSettings(),
    )

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics: StateFlow<Lyrics?> = _lyrics.asStateFlow()

    init {
        playerManager.initialize()
        observeCurrentTrackForLyrics()
    }

    private fun observeCurrentTrackForLyrics() {
        viewModelScope.launch {
            playerManager.state
                .map { it.current?.id }
                .distinctUntilChanged()
                .collect { mediaId ->
                    _lyrics.value = mediaId?.let { repository.getLyrics(it) }
                }
        }
    }

    // ---- Transport --------------------------------------------------------

    fun play(items: List<MediaItem>, startIndex: Int = 0) =
        playerManager.play(items, startIndex)

    fun playSingle(item: MediaItem) = playerManager.playSingle(item)

    fun shuffleAll(items: List<MediaItem>) = playerManager.shuffleAll(items)

    fun togglePlayPause() = playerManager.togglePlayPause()

    fun next() = playerManager.next()

    fun previous() = playerManager.previous()

    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)

    fun seekBy(deltaMs: Long) = playerManager.seekBy(deltaMs)

    fun cycleRepeat() {
        playerManager.cycleRepeatMode()
        viewModelScope.launch {
            val mode = playerManager.state.value.repeatMode
            settingsRepository.update { it.copy(repeatMode = mode) }
        }
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
        viewModelScope.launch {
            val mode = playerManager.state.value.shuffleMode
            settingsRepository.update { it.copy(shuffleMode = mode) }
        }
    }

    fun setSpeed(speed: PlaybackSpeed) {
        playerManager.setSpeed(speed)
        viewModelScope.launch { settingsRepository.update { it.copy(playbackSpeed = speed) } }
    }

    fun setSleepTimer(option: SleepTimerOption) = playerManager.setSleepTimer(option)

    // ---- Queue ------------------------------------------------------------

    fun playNext(items: List<MediaItem>) = playerManager.playNext(items)

    fun addToQueue(items: List<MediaItem>) = playerManager.addToQueue(items)

    fun removeFromQueue(index: Int) = playerManager.removeFromQueue(index)

    fun moveQueueItem(from: Int, to: Int) = playerManager.moveQueueItem(from, to)

    fun clearQueue() = playerManager.clearQueue()

    fun seekToQueueIndex(index: Int) = playerManager.seekToQueueIndex(index)

    fun saveQueueAsPlaylist(name: String) {
        val ids = state.value.queue.map { it.id }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val id = repository.createPlaylist(name)
            repository.addToPlaylist(id, ids)
        }
    }

    // ---- Track actions ----------------------------------------------------

    fun toggleFavourite(item: MediaItem? = state.value.current) {
        val target = item ?: return
        viewModelScope.launch { repository.setFavourite(target.id, !target.isFavourite) }
    }

    fun setRating(rating: Int) {
        val id = state.value.current?.id ?: return
        viewModelScope.launch { repository.setRating(id, rating) }
    }

    fun saveLyrics(lyrics: Lyrics) {
        viewModelScope.launch {
            repository.saveLyrics(lyrics)
            _lyrics.value = lyrics
        }
    }
}
