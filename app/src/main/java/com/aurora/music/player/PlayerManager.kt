package com.aurora.music.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aurora.music.core.common.AppDispatchers
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.PlaybackHistoryEntry
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.PlaybackState
import com.aurora.music.domain.model.PlayerUiState
import com.aurora.music.domain.model.RepeatMode
import com.aurora.music.domain.model.ShuffleMode
import com.aurora.music.domain.model.SleepTimerOption
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.player.service.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * Single source of truth for playback (spec Section 8).
 *
 * Accepts domain [MediaItem]s only and resolves the playable URI internally, so
 * neither the UI nor the queue ever branches on local vs. remote. Wraps a
 * [MediaController] bound to [PlaybackService] — never a second player instance.
 */
@Singleton
@OptIn(UnstableApi::class)
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val dispatchers: AppDispatchers,
) {

    private val scope = CoroutineScope(SupervisorJob()) + dispatchers.main

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    /** Domain queue kept in lock-step with the controller's timeline. */
    private var queue: List<MediaItem> = emptyList()

    private var currentPlayStartedAt: Long = 0L
    private var currentPlayedMs: Long = 0L
    private var lastKnownMediaId: String? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncFromPlayer(player)
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.update {
                it.copy(
                    state = PlaybackState.ERROR,
                    errorMessage = friendlyError(error),
                )
            }
            // Section 8 error handling: skip past an unplayable file rather than dying.
            controller?.let { c ->
                if (c.hasNextMediaItem()) {
                    c.seekToNextMediaItem()
                    c.prepare()
                }
            }
        }
    }

    fun initialize() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                controller = runCatching { future.get() }.getOrNull()?.also { c ->
                    c.addListener(listener)
                    syncFromPlayer(c)
                    startProgressUpdates()
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    fun release() {
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    // ---- Playback commands ------------------------------------------------

    /**
     * Replaces the queue and starts playback at [startIndex].
     * Unplayable items are filtered out so a corrupt file can't stall the queue.
     */
    fun play(items: List<MediaItem>, startIndex: Int = 0, positionMs: Long = 0L) {
        val playable = items.filter { it.isPlayable }
        if (playable.isEmpty()) {
            _state.update { it.copy(errorMessage = "Nothing playable in this selection.") }
            return
        }
        val index = startIndex.coerceIn(0, playable.lastIndex)
        queue = playable
        val controller = controller ?: run {
            // Controller not bound yet: remember intent and apply on connect.
            pendingPlayback = PendingPlayback(playable, index, positionMs)
            initialize()
            return
        }
        controller.setMediaItems(playable.map(::toExoItem), index, positionMs)
        controller.prepare()
        controller.play()
        _state.update {
            it.copy(queue = playable, queueIndex = index, state = PlaybackState.PREPARING)
        }
    }

    fun playSingle(item: MediaItem) = play(listOf(item), 0)

    fun shuffleAll(items: List<MediaItem>) {
        val shuffled = items.filter { it.isPlayable }.shuffled()
        if (shuffled.isEmpty()) return
        setShuffleMode(ShuffleMode.SONGS)
        play(shuffled, 0)
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun playPause(play: Boolean) {
        val c = controller ?: return
        if (play) {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        } else {
            c.pause()
        }
    }

    fun next() {
        val c = controller ?: return
        recordSkipIfNeeded()
        if (c.hasNextMediaItem()) c.seekToNextMediaItem() else c.seekTo(0, 0L)
    }

    fun previous() {
        val c = controller ?: return
        // Standard behaviour: restart the track if we're more than 3s in.
        if (c.currentPosition > 3_000L || !c.hasPreviousMediaItem()) {
            c.seekTo(0L)
        } else {
            c.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        seekTo((c.currentPosition + deltaMs).coerceIn(0L, c.duration.coerceAtLeast(0L)))
    }

    fun seekToQueueIndex(index: Int) {
        val c = controller ?: return
        if (index in queue.indices) {
            c.seekTo(index, 0L)
            c.play()
        }
    }

    fun stop() {
        controller?.stop()
        _state.update { it.copy(state = PlaybackState.STOPPED) }
    }

    // ---- Queue ------------------------------------------------------------

    fun addToQueue(items: List<MediaItem>) {
        val playable = items.filter { it.isPlayable }
        if (playable.isEmpty()) return
        val c = controller ?: run { play(playable); return }
        if (queue.isEmpty()) {
            play(playable)
            return
        }
        c.addMediaItems(playable.map(::toExoItem))
        queue = queue + playable
        _state.update { it.copy(queue = queue) }
    }

    fun playNext(items: List<MediaItem>) {
        val playable = items.filter { it.isPlayable }
        if (playable.isEmpty()) return
        val c = controller ?: run { play(playable); return }
        if (queue.isEmpty()) {
            play(playable)
            return
        }
        val insertAt = (c.currentMediaItemIndex + 1).coerceIn(0, queue.size)
        c.addMediaItems(insertAt, playable.map(::toExoItem))
        queue = queue.toMutableList().apply { addAll(insertAt, playable) }
        _state.update { it.copy(queue = queue) }
    }

    fun removeFromQueue(index: Int) {
        val c = controller ?: return
        if (index !in queue.indices) return
        c.removeMediaItem(index)
        queue = queue.toMutableList().apply { removeAt(index) }
        _state.update { it.copy(queue = queue, queueIndex = c.currentMediaItemIndex) }
    }

    fun moveQueueItem(from: Int, to: Int) {
        val c = controller ?: return
        if (from !in queue.indices || to !in queue.indices || from == to) return
        c.moveMediaItem(from, to)
        queue = queue.toMutableList().apply { add(to, removeAt(from)) }
        _state.update { it.copy(queue = queue, queueIndex = c.currentMediaItemIndex) }
    }

    fun clearQueue() {
        controller?.clearMediaItems()
        queue = emptyList()
        _state.update { it.copy(queue = emptyList(), queueIndex = -1, current = null) }
    }

    // ---- Modes ------------------------------------------------------------

    fun setRepeatMode(mode: RepeatMode) {
        controller?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        _state.update { it.copy(repeatMode = mode) }
    }

    fun cycleRepeatMode() {
        setRepeatMode(
            when (_state.value.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            },
        )
    }

    fun setShuffleMode(mode: ShuffleMode) {
        controller?.shuffleModeEnabled = mode != ShuffleMode.OFF
        _state.update { it.copy(shuffleMode = mode) }
    }

    fun toggleShuffle() {
        setShuffleMode(
            if (_state.value.shuffleMode == ShuffleMode.OFF) {
                ShuffleMode.SONGS
            } else {
                ShuffleMode.OFF
            },
        )
    }

    fun setSpeed(speed: PlaybackSpeed) {
        controller?.setPlaybackSpeed(speed.value)
        _state.update { it.copy(speed = speed) }
    }

    // ---- Sleep timer ------------------------------------------------------

    fun setSleepTimer(option: SleepTimerOption) {
        sleepTimerJob?.cancel()
        when (option) {
            SleepTimerOption.OFF -> {
                _state.update { it.copy(sleepTimerRemainingMs = 0L) }
                return
            }
            SleepTimerOption.END_OF_TRACK, SleepTimerOption.END_OF_QUEUE -> {
                sleepTimerJob = scope.launch { awaitBoundaryThenStop(option) }
                return
            }
            else -> Unit
        }

        val totalMs = option.minutes * 60_000L
        sleepTimerJob = scope.launch {
            var remaining = totalMs
            while (remaining > 0) {
                _state.update { it.copy(sleepTimerRemainingMs = remaining) }
                val step = minOf(1_000L, remaining)
                delay(step)
                remaining -= step
                // Fade out over the final 10 seconds (Section 8).
                if (remaining in 1..10_000) {
                    controller?.volume = (remaining / 10_000f).coerceIn(0f, 1f)
                }
            }
            controller?.pause()
            controller?.volume = 1f
            _state.update { it.copy(sleepTimerRemainingMs = 0L) }
        }
    }

    private suspend fun awaitBoundaryThenStop(option: SleepTimerOption) {
        val startIndex = controller?.currentMediaItemIndex ?: return
        while (true) {
            delay(500)
            val c = controller ?: return
            val ended = when (option) {
                SleepTimerOption.END_OF_TRACK -> c.currentMediaItemIndex != startIndex ||
                    c.playbackState == Player.STATE_ENDED
                else -> c.playbackState == Player.STATE_ENDED
            }
            if (ended) {
                c.pause()
                _state.update { it.copy(sleepTimerRemainingMs = 0L) }
                return
            }
        }
    }

    // ---- Internals --------------------------------------------------------

    private data class PendingPlayback(
        val items: List<MediaItem>,
        val index: Int,
        val positionMs: Long,
    )

    private var pendingPlayback: PendingPlayback? = null

    private fun syncFromPlayer(player: Player) {
        pendingPlayback?.let { pending ->
            pendingPlayback = null
            play(pending.items, pending.index, pending.positionMs)
            return
        }

        val index = player.currentMediaItemIndex
        val current = queue.getOrNull(index)

        if (current?.id != lastKnownMediaId) {
            flushPlaybackStats()
            lastKnownMediaId = current?.id
            currentPlayStartedAt = System.currentTimeMillis()
            currentPlayedMs = 0L
        }

        _state.update {
            it.copy(
                current = current,
                queue = queue,
                queueIndex = index,
                state = player.toPlaybackState(),
                durationMs = player.duration.takeIf { d -> d > 0 } ?: current?.durationMs ?: 0L,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                repeatMode = when (player.repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                },
                shuffleMode = if (player.shuffleModeEnabled &&
                    it.shuffleMode == ShuffleMode.OFF
                ) {
                    ShuffleMode.SONGS
                } else if (!player.shuffleModeEnabled) {
                    ShuffleMode.OFF
                } else {
                    it.shuffleMode
                },
                speed = PlaybackSpeed.nearest(player.playbackParameters.speed),
                errorMessage = if (player.playerError == null) null else it.errorMessage,
            )
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                val c = controller
                if (c != null && c.isPlaying) {
                    currentPlayedMs += 500
                    _state.update {
                        it.copy(
                            positionMs = c.currentPosition.coerceAtLeast(0L),
                            bufferedPositionMs = c.bufferedPosition.coerceAtLeast(0L),
                            durationMs = c.duration.takeIf { d -> d > 0 } ?: it.durationMs,
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun flushPlaybackStats() {
        val mediaId = lastKnownMediaId ?: return
        val listened = currentPlayedMs
        if (listened < 3_000L) return
        val duration = _state.value.durationMs.takeIf { it > 0 } ?: return
        val completion = (listened.toFloat() / duration).coerceIn(0f, 1f)
        scope.launch {
            repository.recordPlayback(
                PlaybackHistoryEntry(
                    mediaId = mediaId,
                    playedAtEpochMillis = currentPlayStartedAt,
                    listenedDurationMs = listened,
                    completionPercent = completion,
                    wasSkipped = completion < 0.5f,
                ),
            )
        }
    }

    private fun recordSkipIfNeeded() {
        val duration = _state.value.durationMs
        if (duration > 0 && currentPlayedMs.toFloat() / duration < 0.5f) {
            flushPlaybackStats()
            currentPlayedMs = 0L
        }
    }

    private fun toExoItem(item: MediaItem): androidx.media3.common.MediaItem {
        val uri = item.playbackUri.orEmpty()
        val extras = Bundle().apply { putString(EXTRA_AURORA_ID, item.id) }
        return androidx.media3.common.MediaItem.Builder()
            .setMediaId(item.id)
            .setUri(Uri.parse(uri))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(item.title)
                    .setArtist(item.artist)
                    .setAlbumTitle(item.album)
                    .setAlbumArtist(item.albumArtist ?: item.artist)
                    .setGenre(item.genre)
                    .setTrackNumber(item.trackNumber.takeIf { it > 0 })
                    .setDiscNumber(item.discNumber.takeIf { it > 0 })
                    .setRecordingYear(item.year.takeIf { it > 0 })
                    .setArtworkUri(item.artworkUri?.let(Uri::parse))
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setExtras(extras)
                    .build(),
            )
            .build()
    }

    private fun Player.toPlaybackState(): PlaybackState = when (playbackState) {
        Player.STATE_IDLE -> if (playerError != null) PlaybackState.ERROR else PlaybackState.IDLE
        Player.STATE_BUFFERING -> PlaybackState.BUFFERING
        Player.STATE_READY -> if (playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
        Player.STATE_ENDED -> PlaybackState.ENDED
        else -> PlaybackState.IDLE
    }

    private fun friendlyError(error: androidx.media3.common.PlaybackException): String =
        when (error.errorCode) {
            androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                "That file is missing. It may have been moved or deleted."
            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NO_PERMISSION ->
                "Aurora no longer has permission to read this file."
            androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED,
            androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            ->
                "This file's format isn't supported on this device."
            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                "Couldn't reach the network. Playing local files still works."
            else -> "Couldn't play this track. Skipping to the next one."
        }

    companion object {
        const val EXTRA_AURORA_ID = "com.aurora.music.MEDIA_ID"
    }
}
