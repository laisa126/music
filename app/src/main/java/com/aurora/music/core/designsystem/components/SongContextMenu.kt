package com.aurora.music.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.music.core.designsystem.montage.MontageBottomSheet
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.MediaItem

@Composable
fun SongContextMenu(
    item: MediaItem, onDismiss: () -> Unit,
    onToggleFavourite: () -> Unit = {}, onPlayNext: () -> Unit = {},
    onAddToQueue: () -> Unit = {}, onAddToPlaylist: () -> Unit = {},
    onGoToAlbum: () -> Unit = {}, onGoToArtist: () -> Unit = {},
    onFileInfo: () -> Unit = {}, onEditMetadata: () -> Unit = {},
    onShare: () -> Unit = {},
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    MontageBottomSheet(visible = true, onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.sm, vertical = MontageSpacing.sm).padding(bottom = MontageSpacing.xxxl)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Artwork(uri = item.artworkUri, contentDescription = item.album, shape = RoundedCornerShape(MontageShapes.icon), modifier = Modifier.size(52.dp))
                Spacer(Modifier.width(MontageSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    MontageText(text = item.title, style = typography.caption, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    MontageText(text = item.artist, style = typography.mini, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(MontageSpacing.xs))
            ContextAction(icon = if (item.isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, label = if (item.isFavourite) "Remove from favourites" else "Add to favourites", onClick = { onToggleFavourite(); onDismiss() })
            ContextAction(icon = Icons.Rounded.SkipNext, label = "Play next", onClick = { onPlayNext(); onDismiss() })
            ContextAction(icon = Icons.Rounded.QueueMusic, label = "Add to queue", onClick = { onAddToQueue(); onDismiss() })
            ContextAction(icon = Icons.Rounded.PlaylistAdd, label = "Add to playlist", onClick = { onAddToPlaylist(); onDismiss() })
            if (item.albumId > 0) ContextAction(icon = Icons.Rounded.Album, label = "Go to album", onClick = { onGoToAlbum(); onDismiss() })
            if (item.artistId > 0) ContextAction(icon = Icons.Rounded.Album, label = "Go to artist", onClick = { onGoToArtist(); onDismiss() })
            ContextAction(icon = Icons.Rounded.Info, label = "File information", onClick = { onFileInfo(); onDismiss() })
            ContextAction(icon = Icons.Rounded.Edit, label = "Edit metadata", onClick = { onEditMetadata(); onDismiss() })
            ContextAction(icon = Icons.Rounded.Share, label = "Share", onClick = { onShare(); onDismiss() })
        }
    }
}

@Composable
private fun ContextAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.small)).clickable(onClick = onClick).padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        MontageIcon(icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(MontageIcons.medium + 2.dp))
        Spacer(Modifier.width(MontageSpacing.lg))
        MontageText(text = label, style = typography.body, color = colors.textPrimary)
    }
}
