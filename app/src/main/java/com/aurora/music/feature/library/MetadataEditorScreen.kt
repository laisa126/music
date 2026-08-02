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
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.LoadingState
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTextField
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons

/**
 * Metadata editor screen for editing track tags (spec Section 9).
 * Edits are saved to the Room database; tag writing to files is a future feature.
 */
@Composable
fun MetadataEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackDetailViewModel = hiltViewModel(),
) {
    val track by viewModel.track.collectAsStateWithLifecycle()
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    // Track-loaded flag to avoid overwriting edits on recomposition
    var loadedId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var albumArtist by remember { mutableStateOf("") }
    var composer by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var trackNumber by remember { mutableStateOf("") }
    var discNumber by remember { mutableStateOf("") }

    // Sync state when track loads or changes
    val t = track
    if (t != null && t.id != loadedId) {
        loadedId = t.id
        title = t.title
        artist = t.artist
        album = t.album
        albumArtist = t.albumArtist ?: ""
        composer = t.composer ?: ""
        genre = t.genre ?: ""
        year = if (t.year > 0) t.year.toString() else ""
        trackNumber = if (t.trackNumber > 0) t.trackNumber.toString() else ""
        discNumber = if (t.discNumber > 0) t.discNumber.toString() else ""
    }

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = "Edit metadata",
                navigationIcon = {
                    MontageIconButton(onClick = onBack) {
                        MontageIcon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
                },
                actions = {
                    MontageIconButton(
                        onClick = {
                            val current = track ?: return@MontageIconButton
                            val updated = current.copy(
                                title = title.trim(),
                                artist = artist.trim(),
                                album = album.trim(),
                                albumArtist = albumArtist.trim().ifBlank { null },
                                composer = composer.trim().ifBlank { null },
                                genre = genre.trim().ifBlank { null },
                                year = year.toIntOrNull() ?: 0,
                                trackNumber = trackNumber.toIntOrNull() ?: 0,
                                discNumber = discNumber.toIntOrNull() ?: 0,
                            )
                            viewModel.updateMetadata(updated)
                            onBack()
                        },
                        enabled = track != null,
                    ) {
                        MontageIcon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "Save",
                            tint = colors.accent,
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

        val currentTrack = track!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(MontageSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(MontageSpacing.md),
        ) {
            // Artwork header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Artwork(
                        uri = currentTrack.artworkUri,
                        contentDescription = currentTrack.album,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(80.dp),
                    )
                    Spacer(Modifier.width(MontageSpacing.base))
                    Column(modifier = Modifier.weight(1f)) {
                        MontageText(
                            text = currentTrack.fileName ?: currentTrack.title,
                            style = typography.labelLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                        )
                        MontageText(
                            text = currentTrack.qualityBadge,
                            style = typography.label,
                            color = colors.textSecondary,
                        )
                    }
                }
            }

            item {
                MontageText(
                    text = "Note: Changes are saved to Aurora's database. Writing tags to " +
                        "files will be available in a future update.",
                    style = typography.caption,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(vertical = MontageSpacing.xs),
                )
            }

            // Editable fields
            item {
                MontageTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Title",
                    leadingIcon = {
                        MontageIcon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(MontageIcons.medium),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                MontageTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    placeholder = "Artist",
                    leadingIcon = {
                        MontageIcon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(MontageIcons.medium),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                MontageTextField(
                    value = album,
                    onValueChange = { album = it },
                    placeholder = "Album",
                    leadingIcon = {
                        MontageIcon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(MontageIcons.medium),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                MontageTextField(
                    value = albumArtist,
                    onValueChange = { albumArtist = it },
                    placeholder = "Album artist",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                MontageTextField(
                    value = composer,
                    onValueChange = { composer = it },
                    placeholder = "Composer",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                MontageTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    placeholder = "Genre",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md),
                ) {
                    MontageTextField(
                        value = year,
                        onValueChange = { year = it },
                        placeholder = "Year",
                        modifier = Modifier.weight(1f),
                    )
                    MontageTextField(
                        value = trackNumber,
                        onValueChange = { trackNumber = it },
                        placeholder = "Track #",
                        modifier = Modifier.weight(1f),
                    )
                    MontageTextField(
                        value = discNumber,
                        onValueChange = { discNumber = it },
                        placeholder = "Disc #",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Save button
            item {
                Spacer(Modifier.height(MontageSpacing.sm))
                MontagePrimaryButton(
                    onClick = {
                        val updated = currentTrack.copy(
                            title = title.trim(),
                            artist = artist.trim(),
                            album = album.trim(),
                            albumArtist = albumArtist.trim().ifBlank { null },
                            composer = composer.trim().ifBlank { null },
                            genre = genre.trim().ifBlank { null },
                            year = year.toIntOrNull() ?: 0,
                            trackNumber = trackNumber.toIntOrNull() ?: 0,
                            discNumber = discNumber.toIntOrNull() ?: 0,
                        )
                        viewModel.updateMetadata(updated)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MontageIcon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null,
                        tint = colors.textOnAccent,
                        modifier = Modifier.size(MontageIcons.small),
                    )
                    Spacer(Modifier.width(MontageSpacing.sm))
                    MontageText(
                        text = "Save changes",
                        style = typography.label,
                        color = colors.textOnAccent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
