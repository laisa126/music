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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.LoadingState

/**
 * Detailed file information screen showing codec, bitrate, sample rate,
 * file size, path and other technical metadata (spec Section 9).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackDetailViewModel = hiltViewModel(),
) {
    val track by viewModel.track.collectAsStateWithLifecycle()

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
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val t = track!!

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
                        uri = t.artworkUri,
                        contentDescription = t.album,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(80.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = t.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                        )
                        Text(
                            text = t.artist,
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
                InfoRow(Icons.Rounded.GraphicEq, "Format", t.qualityBadge)
                InfoRow(Icons.Rounded.AudioFile, "MIME type", t.mimeType ?: "Unknown")
                InfoRow(Icons.Rounded.MusicNote, "Bitrate",
                    if (t.bitrateKbps > 0) "${t.bitrateKbps} kbps" else "Unknown")
                InfoRow(Icons.Rounded.Album, "Sample rate",
                    if (t.sampleRateHz > 0) "${"%.1f".format(t.sampleRateHz / 1000.0)} kHz" else "Unknown")
                InfoRow(Icons.Rounded.Album, "Bit depth",
                    if (t.bitDepth > 0) "${t.bitDepth}-bit" else "Unknown")
                InfoRow(Icons.Rounded.Album, "Channels",
                    when (t.channels) {
                        1 -> "Mono"
                        2 -> "Stereo"
                        6 -> "5.1 surround"
                        in 3..5 -> "${t.channels} channels"
                        else -> if (t.channels > 0) "${t.channels} channels" else "Unknown"
                    })
                InfoRow(Icons.Rounded.Album, "Lossless",
                    if (t.isLossless) "Yes" else "No")
            }

            item { Spacer(Modifier.height(8.dp)) }

            // File section
            item {
                SectionHeader("File")
                InfoRow(Icons.Rounded.AudioFile, "File name", t.fileName ?: "Unknown")
                InfoRow(Icons.Rounded.Info, "File size",
                    if (t.fileSizeBytes > 0) formatFileSize(t.fileSizeBytes) else "Unknown")
                InfoRow(Icons.Rounded.Info, "Path", t.filePath ?: "Unknown")
                InfoRow(Icons.Rounded.Info, "Date added",
                    if (t.dateAddedEpochSeconds > 0)
                        java.text.SimpleDateFormat.getDateTimeInstance()
                            .format(java.util.Date(t.dateAddedEpochSeconds * 1000))
                    else "Unknown")
                InfoRow(Icons.Rounded.Info, "Date modified",
                    if (t.dateModifiedEpochSeconds > 0)
                        java.text.SimpleDateFormat.getDateTimeInstance()
                            .format(java.util.Date(t.dateModifiedEpochSeconds * 1000))
                    else "Unknown")
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Track metadata section
            item {
                SectionHeader("Track metadata")
                InfoRow(Icons.Rounded.MusicNote, "Title", t.title)
                InfoRow(Icons.Rounded.MusicNote, "Artist", t.artist)
                InfoRow(Icons.Rounded.Album, "Album", t.album)
                InfoRow(Icons.Rounded.MusicNote, "Album artist", t.albumArtist ?: "Not set")
                InfoRow(Icons.Rounded.MusicNote, "Composer", t.composer ?: "Not set")
                InfoRow(Icons.Rounded.MusicNote, "Genre", t.genre ?: "Not set")
                InfoRow(Icons.Rounded.Info, "Year",
                    if (t.year > 0) t.year.toString() else "Not set")
                InfoRow(Icons.Rounded.Info, "Track number",
                    if (t.trackNumber > 0) t.trackNumber.toString() else "Not set")
                InfoRow(Icons.Rounded.Info, "Disc number",
                    if (t.discNumber > 0) t.discNumber.toString() else "Not set")
                InfoRow(Icons.Rounded.Info, "Duration",
                    com.aurora.music.core.common.formatDuration(t.durationMs))
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
