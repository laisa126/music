package com.aurora.music.feature.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDuration
import com.aurora.music.core.common.formatRemaining
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.SongRow
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.PlayerUiState
import com.aurora.music.domain.model.RepeatMode
import com.aurora.music.domain.model.ShuffleMode
import com.aurora.music.domain.model.SleepTimerOption
import com.aurora.music.player.AlbumColors
import com.aurora.music.player.rememberAlbumColors

/* ═══════════════════════════════════════════════════════════════════════════
   FLAGSHIP NOW-PLAYING SCREEN
   Inspired by Apple Music on iOS, blended with ColorOS + Material 3.
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
fun PlayerScreen(
    onCollapse: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenFileInfo: (String) -> Unit,
    onOpenMetadataEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val albumColors = rememberAlbumColors(state.current?.artworkUri)

    // Bottom-sheet state
    var showLyrics by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // ---- Animated background ----
        NowPlayingBackground(albumColors = albumColors)

        // ---- Content ----
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar
            NowPlayingTopBar(
                current = state.current,
                queue = state.queue,
                onCollapse = onCollapse,
                onOverflow = { showOverflow = true },
                onShuffle = viewModel::toggleShuffle,
                shuffleMode = state.shuffleMode,
                onRepeat = viewModel::cycleRepeat,
                repeatMode = state.repeatMode,
                onSleepTimer = { showSleepTimer = true },
                onSpeed = { showSpeed = true },
                onEqualizer = { /* navigate or sheet */ },
                onSongInfo = { state.current?.let { onOpenFileInfo(it.id) } },
                onShare = { /* share intent */ },
                onAddToPlaylist = { /* playlist sheet */ },
                onGoToArtist = { state.current?.artistId?.let { onOpenArtist(it) } },
                onGoToAlbum = { state.current?.albumId?.let { onOpenAlbum(it) } },
                onViewFileDetails = { state.current?.let { onOpenFileInfo(it.id) } },
                showOverflow = showOverflow,
                onDismissOverflow = { showOverflow = false },
            )

            Spacer(Modifier.height(24.dp))

            // Artwork
            NowPlayingArtwork(
                state = state,
                albumColors = albumColors,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onCollapse = onCollapse,
                onToggleFavourite = { viewModel.toggleFavourite() },
                onLongPress = { showOverflow = true },
            )

            Spacer(Modifier.height(32.dp))

            // Song info
            NowPlayingSongInfo(
                state = state,
                onToggleFavourite = { viewModel.toggleFavourite() },
                onOpenAlbum = onOpenAlbum,
                onOpenArtist = onOpenArtist,
            )

            Spacer(Modifier.height(28.dp))

            // Progress
            NowPlayingProgress(
                state = state,
                onSeek = viewModel::seekTo,
            )

            Spacer(Modifier.height(36.dp))

            // Primary controls
            NowPlayingControls(
                state = state,
                onTogglePlay = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
            )

            Spacer(Modifier.height(32.dp))

            // Lyrics pill
            if (lyrics != null && !lyrics!!.isEmpty) {
                LyricsPill(onTap = { showLyrics = true })
                Spacer(Modifier.height(20.dp))
            }

            // Up Next card
            if (state.upNext.isNotEmpty()) {
                UpNextCard(
                    next = state.upNext.first(),
                    remaining = state.upNext.size,
                    onTap = { showQueue = true },
                )
            }

            Spacer(
                Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                )
            )
        }
    }

    // ---- Bottom sheets ----
    if (showLyrics) {
        LyricsBottomSheet(
            state = state,
            lyrics = lyrics,
            onDismiss = { showLyrics = false },
        )
    }
    if (showQueue) {
        QueueBottomSheet(
            state = state,
            onPlay = { index -> viewModel.seekToQueueIndex(index) },
            onRemove = { index -> viewModel.removeFromQueue(index) },
            onMoveUp = { from, to -> viewModel.moveQueueItem(from, to) },
            onMoveDown = { from, to -> viewModel.moveQueueItem(from, to) },
            onClear = viewModel::clearQueue,
            onSaveAsPlaylist = { name -> viewModel.saveQueueAsPlaylist(name) },
            onDismiss = { showQueue = false },
        )
    }
    if (showSpeed) {
        SpeedBottomSheet(
            currentSpeed = state.speed,
            onSpeedChange = viewModel::setSpeed,
            onDismiss = { showSpeed = false },
        )
    }
    if (showSleepTimer) {
        SleepTimerBottomSheet(
            currentOption = SleepTimerOption.entries.firstOrNull {
                it.minutes.toLong() == state.sleepTimerRemainingMs / 60_000L
            } ?: SleepTimerOption.OFF,
            onSelect = viewModel::setSleepTimer,
            onDismiss = { showSleepTimer = false },
        )
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   BACKGROUND
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun NowPlayingBackground(albumColors: AlbumColors) {
    val scheme = MaterialTheme.colorScheme
    val animatedPrimary by animateColorAsState(
        targetValue = albumColors.primary,
        animationSpec = tween(800),
        label = "bgPrimary",
    )
    val animatedSecondary by animateColorAsState(
        targetValue = albumColors.secondary,
        animationSpec = tween(800),
        label = "bgSecondary",
    )
    val animatedSurface by animateColorAsState(
        targetValue = albumColors.surface,
        animationSpec = tween(800),
        label = "bgSurface",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            animatedPrimary,
                            animatedSecondary,
                            animatedSurface,
                            scheme.background,
                            scheme.background,
                        ),
                        startY = 0f,
                        endY = size.height,
                    ),
                )
            },
    )
}

/* ═══════════════════════════════════════════════════════════════════════════
   TOP BAR
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun NowPlayingTopBar(
    current: MediaItem?,
    queue: List<MediaItem>,
    onCollapse: () -> Unit,
    onOverflow: () -> Unit,
    onShuffle: () -> Unit,
    shuffleMode: ShuffleMode,
    onRepeat: () -> Unit,
    repeatMode: RepeatMode,
    onSleepTimer: () -> Unit,
    onSpeed: () -> Unit,
    onEqualizer: () -> Unit,
    onSongInfo: () -> Unit,
    onShare: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onGoToArtist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onViewFileDetails: () -> Unit,
    showOverflow: Boolean,
    onDismissOverflow: () -> Unit,
) {
    // Derive "Playing from" label
    val playingFrom = remember(current, queue) {
        val album = current?.album
        if (album.isNullOrBlank()) return@remember "Queue"
        val sameAlbum = queue.take(8).all { it.album == album }
        if (sameAlbum) "Album" else "Queue"
    }
    val playingFromName = current?.album?.takeIf { playingFrom == "Album" } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                Icons.Rounded.ExpandMore,
                contentDescription = "Collapse",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(28.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Playing from $playingFrom",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (playingFromName.isNotBlank()) {
                Text(
                    text = playingFromName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box {
            IconButton(onClick = onOverflow) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "More",
                    tint = Color.White.copy(alpha = 0.9f),
                )
            }
            DropdownMenu(
                expanded = showOverflow,
                onDismissRequest = onDismissOverflow,
            ) {
                OverflowItem("Shuffle ${if (shuffleMode != ShuffleMode.OFF) "On" else "Off"}", onShuffle)
                OverflowItem("Repeat ${when (repeatMode) { RepeatMode.ONE -> "One"; RepeatMode.ALL -> "All"; else -> "Off" }}", onRepeat)
                OverflowItem("Sleep Timer", onSleepTimer)
                OverflowItem("Playback Speed", onSpeed)
                OverflowItem("Equalizer", onEqualizer)
                OverflowItem("Song Information", onSongInfo)
                OverflowItem("Share", onShare)
                OverflowItem("Add to Playlist", onAddToPlaylist)
                OverflowItem("Go to Artist", onGoToArtist)
                OverflowItem("Go to Album", onGoToAlbum)
                OverflowItem("View File Details", onViewFileDetails)
            }
        }
    }
}

@Composable
private fun OverflowItem(label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        onClick = onClick,
    )
}

/* ═══════════════════════════════════════════════════════════════════════════
   ARTWORK
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun NowPlayingArtwork(
    state: PlayerUiState,
    albumColors: AlbumColors,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCollapse: () -> Unit,
    onToggleFavourite: () -> Unit,
    onLongPress: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    // Animated artwork transition
    AnimatedContent(
        targetState = state.current?.id,
        transitionSpec = {
            fadeIn(tween(400)) togetherWith fadeOut(tween(300))
        },
        label = "artworkTransition",
    ) { trackId ->
        val artworkUri = state.current?.artworkUri
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
                .shadow(24.dp, RoundedCornerShape(30.dp), ambientColor = albumColors.accent.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(30.dp))
                .pointerInput(trackId) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -40f) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNext()
                        } else if (dragAmount > 40f) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onPrevious()
                        }
                    }
                }
                .pointerInput(trackId) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 60f) onCollapse()
                    }
                }
                .pointerInput(trackId) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFavourite()
                        },
                        onLongPress = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Artwork(
                uri = artworkUri,
                contentDescription = state.current?.album,
                shape = RoundedCornerShape(30.dp),
                glow = true,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   SONG INFO
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun NowPlayingSongInfo(
    state: PlayerUiState,
    onToggleFavourite: () -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
) {
    val current = state.current ?: return
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Song title
            Text(
                text = current.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            // Artist — clickable
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = current.artist,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            if (current.artistId > 0) onOpenArtist(current.artistId)
                        },
                )
                if (current.album.isNotBlank()) {
                    Text(
                        text = " · ",
                        fontSize = 17.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Text(
                        text = current.album,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                if (current.albumId > 0) onOpenAlbum(current.albumId)
                            },
                    )
                }
            }
        }

        // Heart — burst animation
        val scale by animateFloatAsState(
            targetValue = if (current.isFavourite) 1.2f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "heartScale",
        )
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleFavourite()
            },
        ) {
            Icon(
                imageVector = if (current.isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (current.isFavourite) "Unfavourite" else "Favourite",
                tint = if (current.isFavourite) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp).graphicsLayer { scaleX = scale; scaleY = scale },
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   PROGRESS
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun NowPlayingProgress(
    state: PlayerUiState,
    onSeek: (Long) -> Unit,
) {
    var scrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableStateOf(0f) }

    val displayed = if (scrubbing) scrubPosition else state.progress
    val displayedMs = (displayed * state.durationMs).toLong()

    Column(modifier = Modifier.fillMaxWidth()) {
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
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                bufferedTrackColor = Color.White.copy(alpha = 0.35f),
            ),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = formatDuration(displayedMs),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatRemaining(displayedMs, state.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   PRIMARY CONTROLS
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun NowPlayingControls(
    state: PlayerUiState,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    // Play button scale animation
    var playPressed by remember { mutableStateOf(false) }
    val playScale by animateFloatAsState(
        targetValue = if (playPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "playScale",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous
        IconButton(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPrevious()
        }) {
            Icon(
                Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }

        // Play / Pause — 84dp circle
        Box(
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer { scaleX = playScale; scaleY = playScale }
                .shadow(12.dp, CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        ),
                    ),
                    CircleShape,
                )
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    playPressed = true
                    onTogglePlay()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(42.dp),
            )
        }

        // Next
        IconButton(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onNext()
        }) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   LYRICS PILL
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun LyricsPill(onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onTap)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Lyrics,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Lyrics",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Tap to View",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   UP NEXT CARD
   ═══════════════════════════════════════════════════════════════════════════ */

@Composable
private fun UpNextCard(
    next: MediaItem,
    remaining: Int,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            uri = next.artworkUri,
            contentDescription = next.title,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Up Next",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
            Text(
                text = next.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = next.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "$remaining",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.4f),
        )
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
   BOTTOM SHEETS
   ═══════════════════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsBottomSheet(
    state: PlayerUiState,
    lyrics: com.aurora.music.domain.model.Lyrics?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentLyrics = lyrics

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = state.current?.title ?: "Lyrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            if (currentLyrics == null || currentLyrics.isEmpty) {
                Text(
                    text = "No lyrics found for this track.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (currentLyrics.isSynced) {
                val listState = remember { androidx.compose.foundation.lazy.rememberLazyListState() }
                val activeLine = currentLyrics.synced.indexOfLast {
                    it.timeMs + (currentLyrics.offsetMs) <= state.positionMs
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(currentLyrics.synced, key = { i, line -> "$i" }) { index, line ->
                        val isActive = index == activeLine
                        val color by animateColorAsState(
                            targetValue = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            label = "lyricColor",
                        )
                        Text(
                            text = line.text,
                            style = if (isActive) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.titleMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                Text(
                    text = currentLyrics.plainText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().height(400.dp)
                        .verticalScroll(remember { androidx.compose.foundation.rememberScrollState() }),
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueBottomSheet(
    state: PlayerUiState,
    onPlay: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMoveUp: (Int, Int) -> Unit,
    onMoveDown: (Int, Int) -> Unit,
    onClear: () -> Unit,
    onSaveAsPlaylist: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Queue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onClear) { Text("Clear") }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(state.queue, key = { index, item -> "${item.id}_$index" }) { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SongRow(
                            item = item,
                            isPlaying = index == state.queueIndex,
                            onClick = { onPlay(index) },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(
                                androidx.compose.material.icons.Icons.Rounded.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedBottomSheet(
    currentSpeed: PlaybackSpeed,
    onSpeedChange: (PlaybackSpeed) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text("Playback Speed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            PlaybackSpeed.entries.forEach { speed ->
                val selected = speed == currentSpeed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            else Modifier,
                        )
                        .clickable { onSpeedChange(speed) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = speed.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerBottomSheet(
    currentOption: SleepTimerOption,
    onSelect: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text("Sleep Timer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            SleepTimerOption.entries.forEach { option ->
                val selected = option == currentOption
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (selected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            else Modifier,
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
