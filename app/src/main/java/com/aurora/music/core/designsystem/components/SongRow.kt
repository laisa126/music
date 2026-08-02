package com.aurora.music.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.music.core.common.formatDuration
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.MediaItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(
    item: MediaItem, onClick: () -> Unit, modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null, onMore: (() -> Unit)? = null,
    isPlaying: Boolean = false, showQualityBadge: Boolean = true,
    showDuration: Boolean = true, trackNumber: Int? = null, selected: Boolean = false,
) {
    val haptics = LocalHapticFeedback.current
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.small)).background(
            when { selected -> colors.accentContainer.copy(alpha = 0.55f); isPlaying -> colors.accent.copy(alpha = 0.10f); else -> androidx.compose.ui.graphics.Color.Transparent },
        ).combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick?.let { { haptics.performHapticFeedback(HapticFeedbackType.LongPress); it() } },
        ).padding(horizontal = MontageSpacing.md, vertical = MontageSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (trackNumber != null) {
            MontageText(text = trackNumber.toString(), style = typography.caption, color = colors.textSecondary, modifier = Modifier.width(28.dp))
        } else {
            Artwork(uri = item.artworkUri, contentDescription = item.album, shape = RoundedCornerShape(MontageShapes.icon), modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.width(MontageSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = isPlaying) { Row { MontageIcon(Icons.Rounded.Equalizer, contentDescription = "Now playing", tint = colors.accent, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) } }
                MontageText(text = item.title, style = typography.caption, color = if (isPlaying) colors.accent else colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isFavourite) { MontageIcon(Icons.Rounded.Favorite, contentDescription = null, tint = colors.favourite, modifier = Modifier.size(MontageIcons.tiny)); Spacer(Modifier.width(MontageSpacing.xs)) }
                MontageText(text = item.artist, style = typography.mini, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
        }
        Spacer(Modifier.width(MontageSpacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MontageSpacing.xs)) {
            if (showQualityBadge && item.isLossless) QualityBadge(item.qualityBadge)
            if (showDuration && item.durationMs > 0) MontageText(text = formatDuration(item.durationMs), style = typography.mini, color = colors.textTertiary)
            if (onMore != null) {
                MontageIconButton(onClick = onMore, modifier = Modifier.size(40.dp)) {
                    MontageIcon(Icons.Rounded.MoreVert, contentDescription = "More", tint = colors.textTertiary, modifier = Modifier.size(MontageIcons.medium))
                }
            }
        }
    }
}

@Composable
fun QualityBadge(label: String, modifier: Modifier = Modifier) {
    val colors = MontageTheme.colors
    Box(modifier = modifier.clip(RoundedCornerShape(MontageShapes.badge)).background(colors.accentContainer).padding(horizontal = 6.dp, vertical = 2.dp)) {
        MontageText(text = label, style = MontageTheme.typography.mini, color = colors.accent)
    }
}
