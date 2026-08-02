package com.aurora.music.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.domain.model.MediaItem

/**
 * Detailed file information screen showing codec, bitrate, sample rate,
 * file size, path and other technical metadata (spec Section 9).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val track = state.tracks.firstOrNull()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("File information") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (track == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text("No track selected", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header with artwork
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Artwork(
                        uri = track.artworkUri,
                        contentDescription = track.album,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(80.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Audio section
            item {
                SectionHeader("Audio")
                InfoRow(Icons.Rounded.GraphicEq, "Format", track.qualityBadge)
                InfoRow(Icons.Rounded.AudioFile, "MIME type", track.mimeType ?: "Unknown")
                InfoRow(Icons.Rounded.MusicNote, "Bitrate",
                    if (track.bitrateKbps > 0) "${track.bitrateKbps} kbps" else "Unknown")
                InfoRow(Icons.Rounded.Album, "Sample rate",
                    if (track.sampleRateHz > 0) "${"%.1f".format(track.sampleRateHz / 1000.0)} kHz" else "Unknown")
                InfoRow(Icons.Rounded.Album, "Bit depth",
                    if (track.bitDepth > 0) "${track.bitDepth}-bit" else "Unknown")
                InfoRow(Icons.Rounded.Album, "Channels",
                    when (track.channels) {
                        1 -> "Mono"
                        2 -> "Stereo"
                        6 -> "5.1 surround"
                        in 3..5 -> "${track.channels} channels"
                        else -> if (track.channels > 0) "${track.channels} channels" else "Unknown"
                    })
                InfoRow(Icons.Rounded.Album, "Lossless",
                    if (track.isLossless) "Yes" else "No")
            }

            item { Spacer(Modifier.height(8.dp)) }

            // File section
            item {
                SectionHeader("File")
                InfoRow(Icons.Rounded.AudioFile, "File name",
                    track.fileName ?: "Unknown")
                InfoRow(Icons.Rounded.Info, "File size",
                    if (track.fileSizeBytes > 0) formatFileSize(track.fileSizeBytes) else "Unknown")
                InfoRow(Icons.Rounded.Info, "Path",
                    track.filePath ?: "Unknown")
                InfoRow(Icons.Rounded.Info, "Date added",
                    if (track.dateAddedEpochSeconds > 0)
                        java.text.SimpleDateFormat.getDateTimeInstance()
                            .format(java.util.Date(track.dateAddedEpochSeconds * 1000))
                    else "Unknown")
                InfoRow(Icons.Rounded.Info, "Date modified",
                    if (track.dateModifiedEpochSeconds > 0)
                        java.text.SimpleDateFormat.getDateTimeInstance()
                            .format(java.util.Date(track.dateModifiedEpochSeconds * 1000))
                    else "Unknown")
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Track metadata section
            item {
                SectionHeader("Track metadata")
                InfoRow(Icons.Rounded.MusicNote, "Title", track.title)
                InfoRow(Icons.Rounded.MusicNote, "Artist", track.artist)
                InfoRow(Icons.Rounded.Album, "Album", track.album)
                InfoRow(Icons.Rounded.MusicNote, "Album artist",
                    track.albumArtist ?: "Not set")
                InfoRow(Icons.Rounded.MusicNote, "Composer",
                    track.composer ?: "Not set")
                InfoRow(Icons.Rounded.MusicNote, "Genre",
                    track.genre ?: "Not set")
                InfoRow(Icons.Rounded.Info, "Year",
                    if (track.year > 0) track.year.toString() else "Not set")
                InfoRow(Icons.Rounded.Info, "Track number",
                    if (track.trackNumber > 0) track.trackNumber.toString() else "Not set")
                InfoRow(Icons.Rounded.Info, "Disc number",
                    if (track.discNumber > 0) track.discNumber.toString() else "Not set")
                InfoRow(Icons.Rounded.Info, "Duration",
                    com.aurora.music.core.common.formatDuration(track.durationMs))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
