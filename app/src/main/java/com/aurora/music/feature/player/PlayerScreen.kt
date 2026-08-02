package com.aurora.music.feature.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDuration
import com.aurora.music.core.common.formatRemaining
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.SongRow
import com.aurora.music.core.designsystem.montage.MontageBottomSheet
import com.aurora.music.core.designsystem.montage.MontageCard
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSlider
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.PlayerUiState
import com.aurora.music.domain.model.RepeatMode
import com.aurora.music.domain.model.ShuffleMode
import com.aurora.music.domain.model.SleepTimerOption
import com.aurora.music.player.AlbumColors
import com.aurora.music.player.rememberAlbumColors

@Composable
fun PlayerScreen(
    onCollapse: () -> Unit, onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit, onOpenFileInfo: (String) -> Unit,
    onOpenMetadataEditor: (String) -> Unit, modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val albumColors = rememberAlbumColors(state.current?.artworkUri)
    var showLyrics by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        NowPlayingBackground(albumColors = albumColors)
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).padding(horizontal = MontageSpacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
            NowPlayingTopBar(state = state, onCollapse = onCollapse, onOverflow = { showOverflow = true }, showOverflow = showOverflow, onDismissOverflow = { showOverflow = false }, onShuffle = viewModel::toggleShuffle, onRepeat = viewModel::cycleRepeat, onSleepTimer = { showSleepTimer = true }, onSpeed = { showSpeed = true }, onSongInfo = { state.current?.let { onOpenFileInfo(it.id) } }, onGoToArtist = { state.current?.artistId?.let { onOpenArtist(it) } }, onGoToAlbum = { state.current?.albumId?.let { onOpenAlbum(it) } })
            Spacer(Modifier.height(MontageSpacing.xl))
            NowPlayingArtwork(state = state, albumColors = albumColors, onNext = viewModel::next, onPrevious = viewModel::previous, onCollapse = onCollapse, onToggleFavourite = { viewModel.toggleFavourite() }, onLongPress = { showOverflow = true })
            Spacer(Modifier.height(MontageSpacing.xxl))
            NowPlayingSongInfo(state = state, onToggleFavourite = { viewModel.toggleFavourite() }, onOpenAlbum = onOpenAlbum, onOpenArtist = onOpenArtist)
            Spacer(Modifier.height(28.dp))
            NowPlayingProgress(state = state, onSeek = viewModel::seekTo)
            Spacer(Modifier.height(36.dp))
            NowPlayingControls(state = state, onTogglePlay = viewModel::togglePlayPause, onNext = viewModel::next, onPrevious = viewModel::previous)
            Spacer(Modifier.height(MontageSpacing.xxl))
            if (lyrics != null && !lyrics!!.isEmpty) { LyricsPill(onTap = { showLyrics = true }); Spacer(Modifier.height(MontageSpacing.lg)) }
            if (state.upNext.isNotEmpty()) { UpNextCard(next = state.upNext.first(), remaining = state.upNext.size, onTap = { showQueue = true }) }
            Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp))
        }
    }
    if (showLyrics) { LyricsBottomSheet(state = state, lyrics = lyrics, onDismiss = { showLyrics = false }) }
    if (showQueue) { QueueBottomSheet(state = state, onPlay = { viewModel.seekToQueueIndex(it) }, onRemove = { viewModel.removeFromQueue(it) }, onDismiss = { showQueue = false }) }
    if (showSpeed) { SpeedBottomSheet(currentSpeed = state.speed, onSpeedChange = viewModel::setSpeed, onDismiss = { showSpeed = false }) }
    if (showSleepTimer) { SleepTimerBottomSheet(currentRemainingMs = state.sleepTimerRemainingMs, onSelect = viewModel::setSleepTimer, onDismiss = { showSleepTimer = false }) }
}

@Composable private fun NowPlayingBackground(albumColors: AlbumColors) {
    val bgColor by animateColorAsState(targetValue = albumColors.primary, animationSpec = tween(800), label = "bgColor")
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(bgColor, bgColor.copy(alpha = 0.85f), bgColor.copy(alpha = 0.6f), Color(0xFF0A0A14)))))
}

@Composable
private fun NowPlayingTopBar(state: PlayerUiState, onCollapse: () -> Unit, onOverflow: () -> Unit, showOverflow: Boolean, onDismissOverflow: () -> Unit, onShuffle: () -> Unit, onRepeat: () -> Unit, onSleepTimer: () -> Unit, onSpeed: () -> Unit, onSongInfo: () -> Unit, onGoToArtist: () -> Unit, onGoToAlbum: () -> Unit) {
    val typography = MontageTheme.typography
    val current = state.current
    val playingFrom = remember(current, state.queue) { val album = current?.album; if (album.isNullOrBlank()) "Queue" else if (state.queue.take(8).all { it.album == album }) "Album" else "Queue" }
    val playingFromName = current?.album?.takeIf { playingFrom == "Album" } ?: ""
    Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        MontageIconButton(onClick = onCollapse) { MontageIcon(Icons.Rounded.ExpandMore, contentDescription = "Collapse", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(28.dp)) }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            MontageText(text = "Playing from $playingFrom", style = typography.label, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (playingFromName.isNotBlank()) MontageText(text = playingFromName, style = typography.label, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        MontageIconButton(onClick = onOverflow) { MontageIcon(Icons.Rounded.MoreVert, contentDescription = "More", tint = Color.White.copy(alpha = 0.9f)) }
    }
    if (showOverflow) {
        MontageBottomSheet(visible = true, onDismiss = onDismissOverflow) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.sm, vertical = MontageSpacing.sm).padding(bottom = MontageSpacing.xxxl)) {
                OverflowAction("Shuffle ${if (state.shuffleMode != ShuffleMode.OFF) "On" else "Off"}", onShuffle, onDismissOverflow)
                OverflowAction("Repeat ${when (state.repeatMode) { RepeatMode.ONE -> "One"; RepeatMode.ALL -> "All"; else -> "Off" }}", onRepeat, onDismissOverflow)
                OverflowAction("Sleep Timer", onSleepTimer, onDismissOverflow)
                OverflowAction("Playback Speed", onSpeed, onDismissOverflow)
                OverflowAction("Song Information", onSongInfo, onDismissOverflow)
                OverflowAction("Go to Artist", onGoToArtist, onDismissOverflow)
                OverflowAction("Go to Album", onGoToAlbum, onDismissOverflow)
            }
        }
    }
}

@Composable private fun OverflowAction(label: String, onClick: () -> Unit, onDismiss: () -> Unit) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.small)).clickable { onClick(); onDismiss() }.padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) { MontageText(text = label, style = typography.body, color = colors.textPrimary) }
}

@Composable
private fun NowPlayingArtwork(state: PlayerUiState, albumColors: AlbumColors, onNext: () -> Unit, onPrevious: () -> Unit, onCollapse: () -> Unit, onToggleFavourite: () -> Unit, onLongPress: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    AnimatedContent(targetState = state.current?.id, transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) }, label = "artworkTransition") { trackId ->
        val artworkUri = state.current?.artworkUri
        Box(modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f).shadow(24.dp, RoundedCornerShape(MontageShapes.hero), ambientColor = albumColors.accent.copy(alpha = 0.3f)).clip(RoundedCornerShape(MontageShapes.hero)).pointerInput(trackId) { detectHorizontalDragGestures { _, dragAmount -> if (dragAmount < -40f) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onNext() } else if (dragAmount > 40f) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onPrevious() } } }.pointerInput(trackId) { detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 60f) onCollapse() } }.pointerInput(trackId) { detectTapGestures(onDoubleTap = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onToggleFavourite() }, onLongPress = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onLongPress() }) }, contentAlignment = Alignment.Center) {
            Artwork(uri = artworkUri, contentDescription = state.current?.album, shape = RoundedCornerShape(MontageShapes.hero), glow = true, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun NowPlayingSongInfo(state: PlayerUiState, onToggleFavourite: () -> Unit, onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit) {
    val current = state.current ?: return; val haptics = LocalHapticFeedback.current; val typography = MontageTheme.typography; val colors = MontageTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            MontageText(text = current.title, style = typography.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MontageText(text = current.artist, style = typography.body, color = colors.accent, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { if (current.artistId > 0) onOpenArtist(current.artistId) })
                if (current.album.isNotBlank()) { MontageText(text = " · ", style = typography.body, color = Color.White.copy(alpha = 0.5f)); MontageText(text = current.album, style = typography.caption, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { if (current.albumId > 0) onOpenAlbum(current.albumId) }) }
            }
        }
        val scale by animateFloatAsState(targetValue = if (current.isFavourite) 1.2f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "heartScale")
        MontageIconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onToggleFavourite() }) { MontageIcon(imageVector = if (current.isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = if (current.isFavourite) "Unfavourite" else "Favourite", tint = if (current.isFavourite) colors.favourite else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp).graphicsLayer { scaleX = scale; scaleY = scale }) }
    }
}

@Composable
private fun NowPlayingProgress(state: PlayerUiState, onSeek: (Long) -> Unit) {
    var scrubbing by remember { mutableStateOf(false) }; var scrubPosition by remember { mutableStateOf(0f) }
    val displayed = if (scrubbing) scrubPosition else state.progress
    MontageSlider(value = displayed, onValueChange = { new -> scrubbing = true; scrubPosition = new; onSeek((new * state.durationMs).toLong()) }, modifier = Modifier.fillMaxWidth(), thumbColor = Color.White, trackColor = Color.White.copy(alpha = 0.8f), backgroundColor = Color.White.copy(alpha = 0.2f))
    Row(modifier = Modifier.fillMaxWidth().padding(top = MontageSpacing.xs), horizontalArrangement = Arrangement.SpaceBetween) {
        MontageText(text = formatDuration((displayed * state.durationMs).toLong()), style = MontageTheme.typography.mini, color = Color.White.copy(alpha = 0.5f))
        MontageText(text = formatRemaining((displayed * state.durationMs).toLong(), state.durationMs), style = MontageTheme.typography.mini, color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
private fun NowPlayingControls(state: PlayerUiState, onTogglePlay: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit) {
    val haptics = LocalHapticFeedback.current; val colors = MontageTheme.colors
    val playScale by animateFloatAsState(targetValue = if (state.isPlaying) 1f else 0.92f, animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow), label = "playBtnScale")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        MontageIconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onPrevious() }) { MontageIcon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(32.dp)) }
        Box(modifier = Modifier.size(84.dp).shadow(16.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.15f)).clip(CircleShape).background(Brush.linearGradient(listOf(colors.accent, colors.accentDeep))).clickable { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onTogglePlay() }.graphicsLayer { scaleX = playScale; scaleY = playScale }, contentAlignment = Alignment.Center) { MontageIcon(imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (state.isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.size(40.dp)) }
        MontageIconButton(onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onNext() }) { MontageIcon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(32.dp)) }
    }
}

@Composable
private fun LyricsPill(onTap: () -> Unit) {
    val typography = MontageTheme.typography
    MontageCard(shape = RoundedCornerShape(50), background = Color.White.copy(alpha = 0.12f)) {
        Row(modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onTap).padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            MontageIcon(Icons.Rounded.Lyrics, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(MontageIcons.medium))
            Spacer(Modifier.width(MontageSpacing.sm))
            MontageText(text = "♪ Lyrics", style = typography.label, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun UpNextCard(next: MediaItem, remaining: Int, onTap: () -> Unit) {
    val typography = MontageTheme.typography
    MontageCard(shape = RoundedCornerShape(MontageShapes.card), background = Color.White.copy(alpha = 0.10f)) {
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.card)).clickable(onClick = onTap).padding(MontageSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Artwork(uri = next.artworkUri, contentDescription = next.album, shape = RoundedCornerShape(MontageShapes.icon), modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(MontageSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                MontageText(text = "Up Next", style = typography.mini, color = Color.White.copy(alpha = 0.5f))
                MontageText(text = next.title, style = typography.caption, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                MontageText(text = next.artist, style = typography.mini, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            MontageText(text = "$remaining", style = typography.mini, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun LyricsBottomSheet(state: PlayerUiState, lyrics: com.aurora.music.domain.model.Lyrics?, onDismiss: () -> Unit) {
    val typography = MontageTheme.typography; val colors = MontageTheme.colors; val currentLyrics = lyrics
    MontageBottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.xl, vertical = MontageSpacing.md).padding(bottom = MontageSpacing.xxxl)) {
            MontageText(text = state.current?.title ?: "Lyrics", style = typography.heading, color = colors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(MontageSpacing.md))
            if (currentLyrics == null || currentLyrics.isEmpty) { MontageText(text = "No lyrics found for this track.", style = typography.caption, color = colors.textSecondary) }
            else if (currentLyrics.isSynced) {
                val listState = rememberLazyListState(); val activeLine = currentLyrics.synced.indexOfLast { it.timeMs + (currentLyrics.offsetMs) <= state.positionMs }
                LazyColumn(state = listState, modifier = Modifier.fillMaxWidth().height(400.dp), verticalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
                    itemsIndexed(currentLyrics.synced, key = { i, line -> "$i" }) { index, line ->
                        val isActive = index == activeLine; val color by animateColorAsState(targetValue = if (isActive) colors.accent else colors.textSecondary.copy(alpha = 0.5f), label = "lyricColor")
                        MontageText(text = line.text, style = if (isActive) typography.heading else typography.body, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = color, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            } else { MontageText(text = currentLyrics.plainText.orEmpty(), style = typography.body, color = colors.textPrimary, modifier = Modifier.fillMaxWidth().height(400.dp).verticalScroll(rememberScrollState())) }
            Spacer(Modifier.height(MontageSpacing.xxxl))
        }
    }
}

@Composable
private fun QueueBottomSheet(state: PlayerUiState, onPlay: (Int) -> Unit, onRemove: (Int) -> Unit, onDismiss: () -> Unit) {
    val typography = MontageTheme.typography; val colors = MontageTheme.colors
    MontageBottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.xl, vertical = MontageSpacing.sm), verticalAlignment = Alignment.CenterVertically) { MontageText(text = "Queue", style = typography.heading, color = colors.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) }
            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp), contentPadding = PaddingValues(bottom = MontageSpacing.xxl)) {
                itemsIndexed(state.queue, key = { index, item -> "${item.id}_$index" }) { index, item -> Row(verticalAlignment = Alignment.CenterVertically) { SongRow(item = item, isPlaying = index == state.queueIndex, onClick = { onPlay(index) }, modifier = Modifier.weight(1f)) } }
            }
        }
    }
}

@Composable
private fun SpeedBottomSheet(currentSpeed: PlaybackSpeed, onSpeedChange: (PlaybackSpeed) -> Unit, onDismiss: () -> Unit) {
    val typography = MontageTheme.typography; val colors = MontageTheme.colors
    MontageBottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.xl, vertical = MontageSpacing.md).padding(bottom = MontageSpacing.xxxl)) {
            MontageText(text = "Playback Speed", style = typography.heading, color = colors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(MontageSpacing.md))
            PlaybackSpeed.entries.forEach { speed -> val selected = speed == currentSpeed; Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.small)).then(if (selected) Modifier.background(colors.accentContainer) else Modifier).clickable { onSpeedChange(speed) }.padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md)) { MontageText(text = speed.label, style = typography.body, color = if (selected) colors.accent else colors.textPrimary) } }
            Spacer(Modifier.height(MontageSpacing.xxxl))
        }
    }
}

@Composable
private fun SleepTimerBottomSheet(currentRemainingMs: Long, onSelect: (SleepTimerOption) -> Unit, onDismiss: () -> Unit) {
    val typography = MontageTheme.typography; val colors = MontageTheme.colors
    val currentOption = when {
        currentRemainingMs <= 0L -> SleepTimerOption.OFF
        currentRemainingMs <= 10 * 60 * 1000L -> SleepTimerOption.TEN
        currentRemainingMs <= 15 * 60 * 1000L -> SleepTimerOption.FIFTEEN
        currentRemainingMs <= 30 * 60 * 1000L -> SleepTimerOption.THIRTY
        currentRemainingMs <= 45 * 60 * 1000L -> SleepTimerOption.FORTY_FIVE
        currentRemainingMs <= 60 * 60 * 1000L -> SleepTimerOption.SIXTY
        currentRemainingMs <= 90 * 60 * 1000L -> SleepTimerOption.NINETY
        else -> SleepTimerOption.ONE_TWENTY
    }
    val typography = MontageTheme.typography; val colors = MontageTheme.colors
    MontageBottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.xl, vertical = MontageSpacing.md).padding(bottom = MontageSpacing.xxxl)) {
            MontageText(text = "Sleep Timer", style = typography.heading, color = colors.textPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(MontageSpacing.md))
            SleepTimerOption.entries.forEach { option -> val selected = option == currentOption; Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.small)).then(if (selected) Modifier.background(colors.accentContainer) else Modifier).clickable { onSelect(option) }.padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md)) { MontageText(text = option.label, style = typography.body, color = if (selected) colors.accent else colors.textPrimary) } }
            Spacer(Modifier.height(MontageSpacing.xxxl))
        }
    }
}
