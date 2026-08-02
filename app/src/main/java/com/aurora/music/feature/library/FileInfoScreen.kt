package com.aurora.music.feature.library

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.LoadingState
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons

/**
 * Detailed file information screen showing codec, bitrate, sample rate,
 * file size, path and other technical metadata (spec Section 9).
 */
@Composable
fun FileInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackDetailViewModel = hiltViewModel(),
) {
    val track by viewModel.track.collectAsStateWithLifecycle()
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = "File information",
                navigationIcon = {
                    MontageIconButton(onClick = onBack) {
                        MontageIcon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
                },
            )
        },
        containerColor = colors.background,
    ) { padding ->
        if (track == null) {
            LoadingState(modifier = Modifier.padding(top = padding.calculateTopPadding()))
            return@MontageScaffold
        }

        val t = track!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(MontageSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(MontageSpacing.sm),
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
                    Spacer(Modifier.width(MontageSpacing.base))
                    Column(modifier = Modifier.weight(1f)) {
                        MontageText(
                            text = t.title,
                            style = typography.heading,
                            color = colors.textPrimary,
                            maxLines = 2,
                        )
                        MontageText(
                            text = t.artist,
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(MontageSpacing.sm)) }

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

            item { Spacer(Modifier.height(MontageSpacing.sm)) }

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

            item { Spacer(Modifier.height(MontageSpacing.sm)) }

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
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    MontageText(
        text = title,
        style = typography.labelLarge,
        color = colors.accent,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        modifier = Modifier.padding(top = MontageSpacing.sm, bottom = MontageSpacing.xs),
    )
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MontageSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MontageIcon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(MontageIcons.small),
        )
        Spacer(Modifier.width(MontageSpacing.md))
        MontageText(
            text = label,
            style = typography.body,
            color = colors.textSecondary,
            modifier = Modifier.width(100.dp),
        )
        MontageText(
            text = value,
            style = typography.body,
            color = colors.textPrimary,
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
