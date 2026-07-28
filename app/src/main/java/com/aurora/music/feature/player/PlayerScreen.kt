package com.aurora.music.feature.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDuration
import com.aurora.music.core.common.formatRemaining
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.toComposeShape
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.PlayerUiState
import com.aurora.music.domain.model.RepeatMode
import com.aurora.music.domain.model.ShuffleMode
import com.aurora.music.domain.repository.AppSettings

@Composable
fun PlayerScreen(
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Landscape / large screens get artwork-left, controls-right (Section 12).
        val isWide = maxWidth > maxHeight

        PlayerBackground(state = state, settings = settings)

        if (isWide) {
            LandscapePlayer(
                state = state,
                settings = settings,
                viewModel = viewModel,
                onCollapse = onCollapse,
                onOpenQueue = onOpenQueue,
                onOpenLyrics = onOpenLyrics,
                onOpenEqualizer = onOpenEqualizer,
                onOpenSleepTimer = onOpenSleepTimer,
            )
        } else {
            PortraitPlayer(
                state = state,
                settings = settings,
                viewModel = viewModel,
                onCollapse = onCollapse,
                onOpenQueue = onOpenQueue,
                onOpenLyrics = onOpenLyrics,
                onOpenEqualizer = onOpenEqualizer,
                onOpenSleepTimer = onOpenSleepTimer,
            )
        }
    }
}

@Composable
private fun PlayerBackground(state: PlayerUiState, settings: AppSettings) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        scheme.primary.copy(alpha = if (settings.blurredPlayerBackground) 0.22f else 0.10f),
                        scheme.background,
                        scheme.background,
                    ),
                ),
            ),
    )
}

@Composable
private fun PortraitPlayer(
    state: PlayerUiState,
    settings: AppSettings,
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        PlayerTopBar(
            queueName = "Now Playing",
            onCollapse = onCollapse,
            onMore = onOpenEqualizer,
        )

        Spacer(Modifier.height(12.dp))

        PlayerArtwork(
            state = state,
            settings = settings,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onCollapse = onCollapse,
            onToggleFavourite = { viewModel.toggleFavourite() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .aspectRatio(1f),
        )

        Spacer(Modifier.height(28.dp))

        TrackInfo(state = state, onToggleFavourite = { viewModel.toggleFavourite() })

        Spacer(Modifier.height(16.dp))

        SeekBar(
            state = state,
            onSeek = viewModel::seekTo,
        )

        Spacer(Modifier.height(8.dp))

        TransportControls(
            state = state,
            onTogglePlay = viewModel::togglePlayPause,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onRewind = { viewModel.seekBy(-10_000) },
            onCycleRepeat = viewModel::cycleRepeat,
            onToggleShuffle = viewModel::toggleShuffle,
        )

        Spacer(Modifier.height(12.dp))

        SecondaryActions(
            state = state,
            onOpenQueue = onOpenQueue,
            onOpenLyrics = onOpenLyrics,
            onOpenEqualizer = onOpenEqualizer,
            onOpenSleepTimer = onOpenSleepTimer,
            onCycleSpeed = {
                val entries = PlaybackSpeed.entries
                val next = entries[(entries.indexOf(state.speed) + 1) % entries.size]
                viewModel.setSpeed(next)
            },
            onRewind = { viewModel.seekBy(-10_000) },
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LandscapePlayer(
    state: PlayerUiState,
    settings: AppSettings,
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerArtwork(
            state = state,
            settings = settings,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onCollapse = onCollapse,
            onToggleFavourite = { viewModel.toggleFavourite() },
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f),
        )
        Spacer(Modifier.width(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            PlayerTopBar(
                queueName = "Now Playing",
                onCollapse = onCollapse,
                onMore = onOpenEqualizer,
            )
            Spacer(Modifier.height(8.dp))
            TrackInfo(state = state, onToggleFavourite = { viewModel.toggleFavourite() })
            Spacer(Modifier.height(12.dp))
            SeekBar(state = state, onSeek = viewModel::seekTo)
            TransportControls(
                state = state,
                onTogglePlay = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onRewind = { viewModel.seekBy(-10_000) },
                onCycleRepeat = viewModel::cycleRepeat,
                onToggleShuffle = viewModel::toggleShuffle,
            )
            SecondaryActions(
                state = state,
                onOpenQueue = onOpenQueue,
                onOpenLyrics = onOpenLyrics,
                onOpenEqualizer = onOpenEqualizer,
                onOpenSleepTimer = onOpenSleepTimer,
                onCycleSpeed = {
                    val entries = PlaybackSpeed.entries
                    val next = entries[(entries.indexOf(state.speed) + 1) % entries.size]
                    viewModel.setSpeed(next)
                },
                onRewind = { viewModel.seekBy(-10_000) },
            )
        }
    }
}

@Composable
private fun PlayerTopBar(queueName: String, onCollapse: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(Icons.Rounded.ExpandMore, contentDescription = "Minimize player")
        }
        Text(
            text = queueName,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onMore) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
        }
    }
}

@Composable
private fun PlayerArtwork(
    state: PlayerUiState,
    settings: AppSettings,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCollapse: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    var fullscreen by remember { mutableStateOf(false) }

    // Optional rotating artwork (Section 8) — respects the motion setting.
    val transition = rememberInfiniteTransition(label = "artworkRotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24_000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart,
        ),
        label = "artworkRotationValue",
    )
    val shouldRotate = settings.rotatingArtwork && state.isPlaying

    Box(
        modifier = modifier
            .pointerInput(state.current?.id) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -40f) onNext() else if (dragAmount > 40f) onPrevious()
                }
            }
            .pointerInput(state.current?.id) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 60f) onCollapse()
                }
            }
            .pointerInput(state.current?.id) {
                detectTapGestures(
                    onDoubleTap = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavourite()
                    },
                    onLongPress = { fullscreen = !fullscreen },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Artwork(
            uri = state.current?.artworkUri,
            contentDescription = state.current?.album,
            shape = settings.artworkShape.toComposeShape(),
            glow = true,
            modifier = Modifier
                .fillMaxSize()
                .then(if (shouldRotate) Modifier.rotate(rotation) else Modifier),
        )
    }
}

@Composable
private fun TrackInfo(state: PlayerUiState, onToggleFavourite: () -> Unit) {
    val current = state.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = current?.title ?: "Nothing playing",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = listOfNotNull(
                    current?.artist,
                    current?.album?.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (current != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = listOfNotNull(
                        current.qualityBadge,
                        current.bitrateKbps.takeIf { it > 0 }?.let { "$it kbps" },
                        current.sampleRateHz.takeIf { it > 0 }
                            ?.let { "${"%.1f".format(it / 1000.0)} kHz" },
                        current.year.takeIf { it > 0 }?.toString(),
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
            }
        }
        IconButton(onClick = onToggleFavourite) {
            Icon(
                imageVector = if (current?.isFavourite == true) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Rounded.FavoriteBorder
                },
                contentDescription = "Favourite",
                tint = if (current?.isFavourite == true) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun SeekBar(state: PlayerUiState, onSeek: (Long) -> Unit) {
    var scrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }

    val displayed = if (scrubbing) scrubPosition else state.progress
    val displayedMs = (displayed * state.durationMs).toLong()

    Column {
        Slider(
            value = displayed.coerceIn(0f, 1f),
            onValueChange = {
                scrubbing = true
                scrubPosition = it
            },
            onValueChangeFinished = {
                onSeek((scrubPosition * state.durationMs).toLong())
                scrubbing = false
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            ),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = formatDuration(displayedMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatRemaining(displayedMs, state.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransportControls(
    state: PlayerUiState,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleShuffle) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (state.shuffleMode != ShuffleMode.OFF) {
                    scheme.primary
                } else {
                    scheme.onSurfaceVariant
                },
            )
        }
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(34.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(scheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTogglePlay()
                },
                modifier = Modifier.size(72.dp),
            ) {
                Icon(
                    imageVector = if (state.isPlaying) {
                        Icons.Rounded.Pause
                    } else {
                        Icons.Rounded.PlayArrow
                    },
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        IconButton(onClick = onNext) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(34.dp),
            )
        }
        IconButton(onClick = onCycleRepeat) {
            Icon(
                imageVector = if (state.repeatMode == RepeatMode.ONE) {
                    Icons.Rounded.RepeatOne
                } else {
                    Icons.Rounded.Repeat
                },
                contentDescription = "Repeat",
                tint = if (state.repeatMode != RepeatMode.OFF) {
                    scheme.primary
                } else {
                    scheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun SecondaryActions(
    state: PlayerUiState,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onCycleSpeed: () -> Unit,
    onRewind: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SmallAction(Icons.Rounded.Replay10, "Rewind 10 seconds", onRewind)
        SmallAction(Icons.Rounded.Lyrics, "Lyrics", onOpenLyrics)
        SmallAction(Icons.AutoMirrored.Rounded.QueueMusic, "Queue", onOpenQueue)
        SmallAction(Icons.Rounded.Equalizer, "Equalizer", onOpenEqualizer)
        SmallAction(Icons.Rounded.Bedtime, "Sleep timer", onOpenSleepTimer)
        Box(contentAlignment = Alignment.Center) {
            IconButton(onClick = onCycleSpeed) {
                Icon(
                    Icons.Rounded.Speed,
                    contentDescription = "Playback speed ${state.speed.label}",
                    tint = if (state.speed != PlaybackSpeed.X1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun SmallAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
