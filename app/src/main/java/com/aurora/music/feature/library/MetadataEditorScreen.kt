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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.domain.model.MediaItem

/**
 * Metadata editor screen for editing track tags (spec Section 9).
 * Edits are saved to the Room database; tag writing to files is a future feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val track = state.tracks.firstOrNull()

    var title by remember(track?.title) { mutableStateOf(track?.title ?: "") }
    var artist by remember(track?.artist) { mutableStateOf(track?.artist ?: "") }
    var album by remember(track?.album) { mutableStateOf(track?.album ?: "") }
    var albumArtist by remember(track?.albumArtist) { mutableStateOf(track?.albumArtist ?: "") }
    var composer by remember(track?.composer) { mutableStateOf(track?.composer ?: "") }
    var genre by remember(track?.genre) { mutableStateOf(track?.genre ?: "") }
    var year by remember(track?.year) { mutableStateOf(track?.year?.toString() ?: "") }
    var trackNumber by remember(track?.trackNumber) { mutableStateOf(track?.trackNumber?.toString() ?: "") }
    var discNumber by remember(track?.discNumber) { mutableStateOf(track?.discNumber?.toString() ?: "") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Edit metadata") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val current = track ?: return@IconButton
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
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = "Save")
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
                    Icons.Rounded.Edit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Artwork header
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
                            text = track.fileName ?: track.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                        )
                        Text(
                            text = track.qualityBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Note: Changes are saved to Aurora's database. Writing tags to " +
                        "files will be available in a future update.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // Editable fields
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    leadingIcon = { Icon(Icons.Rounded.MusicNote, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    leadingIcon = { Icon(Icons.Rounded.MusicNote, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    leadingIcon = { Icon(Icons.Rounded.Album, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = albumArtist,
                    onValueChange = { albumArtist = it },
                    label = { Text("Album artist") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = composer,
                    onValueChange = { composer = it },
                    label = { Text("Composer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = trackNumber,
                        onValueChange = { trackNumber = it },
                        label = { Text("Track #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = discNumber,
                        onValueChange = { discNumber = it },
                        label = { Text("Disc #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }

            // Save button
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val updated = track.copy(
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
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save changes")
                }
            }
        }
    }
}
