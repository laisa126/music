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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.aurora.music.core.designsystem.theme.LocalAuroraTokens
import com.aurora.music.domain.model.PlayerUiState

/**
 * Always sits above the bottom navigation while audio is active (Section 3).
 * Supports tap-to-expand *and* a velocity-aware upward drag (Section 12).
 */
@Composable
fun MiniPlayer(
    state: PlayerUiState,
    onExpand: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = state.current ?: return
    val scheme = MaterialTheme.colorScheme
    val tokens = LocalAuroraTokens.current
    val haptics = LocalHapticFeedback.current

    val progress by animateFloatAsState(
        targetValue = state.progress,
        label = "miniPlayerProgress",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .glassSurface(shape = RoundedCornerShape(22.dp), alpha = 0.94f)
            .clickable(onClick = onExpand)
            .pointerInput(Unit) {
                var accumulated = 0f
                detectVerticalDragGestures(
                    onDragStart = { accumulated = 0f },
                    onDragEnd = {
                        // Threshold + direction check so a stray scroll doesn't expand.
                        if (accumulated < -48f) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onExpand()
                        }
                    },
                ) { _, dragAmount -> accumulated += dragAmount }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                uri = current.artworkUri,
                contentDescription = current.album,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(46.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = current.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = current.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggleFavourite) {
                Icon(
                    imageVector = if (current.isFavourite) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Rounded.FavoriteBorder
                    },
                    contentDescription = if (current.isFavourite) "Unfavourite" else "Favourite",
                    tint = if (current.isFavourite) scheme.tertiary else scheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (state.isPlaying) {
                        Icons.Rounded.Pause
                    } else {
                        Icons.Rounded.PlayArrow
                    },
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = scheme.onSurface,
                    modifier = Modifier.size(30.dp),
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = scheme.onSurface,
                )
            }
        }

        // Hairline progress bar hugging the bottom edge.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(scheme.onSurface.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (tokens.animationsEnabled) progress else state.progress)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(scheme.primary),
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}
