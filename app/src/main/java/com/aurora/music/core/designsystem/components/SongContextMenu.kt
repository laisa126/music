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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.music.domain.model.MediaItem

/**
 * Contextual bottom sheet for song actions (spec Section 9).
 * Appears on long-press or "more" tap on any SongRow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongContextMenu(
    item: MediaItem,
    onDismiss: () -> Unit,
    onToggleFavourite: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
    onGoToArtist: () -> Unit = {},
    onFileInfo: () -> Unit = {},
    onEditMetadata: () -> Unit = {},
    onShare: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 32.dp),
        ) {
            // Track header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(
                    uri = item.artworkUri,
                    contentDescription = item.album,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(52.dp),
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Actions
            ContextAction(
                icon = if (item.isFavourite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (item.isFavourite) "Remove from favourites" else "Add to favourites",
                onClick = { onToggleFavourite(); onDismiss() },
            )
            ContextAction(
                icon = Icons.Rounded.SkipNext,
                label = "Play next",
                onClick = { onPlayNext(); onDismiss() },
            )
            ContextAction(
                icon = Icons.Rounded.QueueMusic,
                label = "Add to queue",
                onClick = { onAddToQueue(); onDismiss() },
            )
            ContextAction(
                icon = Icons.Rounded.PlaylistAdd,
                label = "Add to playlist",
                onClick = { onAddToPlaylist(); onDismiss() },
            )
            if (item.albumId > 0) {
                ContextAction(
                    icon = Icons.Rounded.Album,
                    label = "Go to album",
                    onClick = { onGoToAlbum(); onDismiss() },
                )
            }
            if (item.artistId > 0) {
                ContextAction(
                    icon = Icons.Rounded.Album,
                    label = "Go to artist",
                    onClick = { onGoToArtist(); onDismiss() },
                )
            }
            ContextAction(
                icon = Icons.Rounded.Info,
                label = "File information",
                onClick = { onFileInfo(); onDismiss() },
            )
            ContextAction(
                icon = Icons.Rounded.Edit,
                label = "Edit metadata",
                onClick = { onEditMetadata(); onDismiss() },
            )
            ContextAction(
                icon = Icons.Rounded.Share,
                label = "Share",
                onClick = { onShare(); onDismiss() },
            )
        }
    }
}

@Composable
private fun ContextAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
