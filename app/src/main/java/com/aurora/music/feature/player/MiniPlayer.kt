package com.aurora.music.feature.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.PlayerUiState

@Composable
fun MiniPlayer(
    state: PlayerUiState, onExpand: () -> Unit, onTogglePlay: () -> Unit,
    onNext: () -> Unit, onToggleFavourite: () -> Unit, modifier: Modifier = Modifier,
) {
    val current = state.current ?: return
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    val haptics = LocalHapticFeedback.current
    val progress by animateFloatAsState(targetValue = state.progress, label = "miniPlayerProgress")

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp).clip(RoundedCornerShape(MontageShapes.miniPlayer)).glassSurface(shape = RoundedCornerShape(MontageShapes.miniPlayer), alpha = 0.94f).clickable(onClick = onExpand).pointerInput(Unit) {
            var accumulated = 0f
            detectVerticalDragGestures(onDragStart = { accumulated = 0f }, onDragEnd = { if (accumulated < -48f) { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onExpand() } }) { _, dragAmount -> accumulated += dragAmount }
        },
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.sm, vertical = MontageSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Artwork(uri = current.artworkUri, contentDescription = current.album, shape = RoundedCornerShape(MontageShapes.icon), modifier = Modifier.size(46.dp))
            Spacer(Modifier.width(MontageSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                MontageText(text = current.title, style = typography.caption, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                MontageText(text = current.artist, style = typography.mini, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            MontageIconButton(onClick = onToggleFavourite) { MontageIcon(imageVector = if (current.isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = if (current.isFavourite) "Unfavourite" else "Favourite", tint = if (current.isFavourite) colors.favourite else colors.textSecondary, modifier = Modifier.size(MontageIcons.medium)) }
            MontageIconButton(onClick = onTogglePlay) { MontageIcon(imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (state.isPlaying) "Pause" else "Play", tint = colors.textPrimary, modifier = Modifier.size(30.dp)) }
            MontageIconButton(onClick = onNext) { MontageIcon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next", tint = colors.textPrimary, modifier = Modifier.size(MontageIcons.large)) }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.md).height(2.dp).clip(RoundedCornerShape(1.dp)).background(colors.border)) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(2.dp).clip(RoundedCornerShape(1.dp)).background(colors.accent))
        }
        Spacer(Modifier.height(6.dp))
    }
}
