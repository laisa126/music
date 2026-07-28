package com.aurora.music.domain.model

/** Every playback state called out in Section 8. */
enum class PlaybackState {
    IDLE,
    LOADING,
    BUFFERING,
    PREPARING,
    PLAYING,
    PAUSED,
    STOPPED,
    ENDED,
    ERROR,
    SEEKING,
    ;

    val isActive: Boolean get() = this == PLAYING || this == BUFFERING || this == PREPARING
}

enum class RepeatMode { OFF, ONE, ALL }

enum class ShuffleMode { OFF, SONGS, ALBUMS, ARTISTS, SMART }

enum class PlaybackSpeed(val value: Float, val label: String) {
    X0_5(0.5f, "0.5x"),
    X0_75(0.75f, "0.75x"),
    X1(1.0f, "1.0x"),
    X1_25(1.25f, "1.25x"),
    X1_5(1.5f, "1.5x"),
    X1_75(1.75f, "1.75x"),
    X2(2.0f, "2.0x"),
    ;

    companion object {
        fun nearest(value: Float): PlaybackSpeed =
            entries.minByOrNull { kotlin.math.abs(it.value - value) } ?: X1
    }
}

enum class CrossfadeDuration(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    ONE(1, "1s"),
    TWO(2, "2s"),
    THREE(3, "3s"),
    FIVE(5, "5s"),
    EIGHT(8, "8s"),
    TEN(10, "10s"),
    ;

    companion object {
        fun fromSeconds(seconds: Int): CrossfadeDuration =
            entries.firstOrNull { it.seconds == seconds } ?: OFF
    }
}

enum class SleepTimerOption(val minutes: Int, val label: String) {
    OFF(0, "Off"),
    TEN(10, "10 minutes"),
    FIFTEEN(15, "15 minutes"),
    THIRTY(30, "30 minutes"),
    FORTY_FIVE(45, "45 minutes"),
    SIXTY(60, "1 hour"),
    NINETY(90, "1.5 hours"),
    ONE_TWENTY(120, "2 hours"),
    END_OF_TRACK(-1, "End of track"),
    END_OF_QUEUE(-2, "End of queue"),
}

/** Immutable snapshot of everything the UI needs to render playback. */
data class PlayerUiState(
    val current: MediaItem? = null,
    val queue: List<MediaItem> = emptyList(),
    val queueIndex: Int = -1,
    val state: PlaybackState = PlaybackState.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val speed: PlaybackSpeed = PlaybackSpeed.X1,
    val sleepTimerRemainingMs: Long = 0L,
    val errorMessage: String? = null,
) {
    val isPlaying: Boolean get() = state == PlaybackState.PLAYING
    val hasCurrent: Boolean get() = current != null

    val progress: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    val bufferedProgress: Float
        get() = if (durationMs > 0L) {
            (bufferedPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        } else {
            0f
        }

    val upNext: List<MediaItem>
        get() = if (queueIndex >= 0 && queueIndex < queue.lastIndex) {
            queue.subList(queueIndex + 1, queue.size)
        } else {
            emptyList()
        }

    val history: List<MediaItem>
        get() = if (queueIndex > 0) queue.subList(0, queueIndex) else emptyList()
}

/** Audio output device currently in use (Section 8). */
enum class AudioOutputType { SPEAKER, WIRED_HEADSET, BLUETOOTH, USB_DAC, UNKNOWN }

data class AudioOutput(
    val type: AudioOutputType = AudioOutputType.SPEAKER,
    val deviceName: String? = null,
    val codec: String? = null,
    val sampleRateHz: Int = 0,
    val bitDepth: Int = 0,
)

enum class HeadsetDisconnectBehaviour { PAUSE, RESUME_ON_RECONNECT, IGNORE }

enum class AudioFocusBehaviour { PAUSE, DUCK, IGNORE }
