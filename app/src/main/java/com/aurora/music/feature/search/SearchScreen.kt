package com.aurora.music.feature.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatTrackCount
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.AuroraEmptyState
import com.aurora.music.core.designsystem.components.NoResultsState
import com.aurora.music.core.designsystem.components.SongRow
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.feature.player.PlayerViewModel

@Composable
fun SearchScreen(
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onOpenFolder: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val recents by viewModel.recentSearches.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            viewModel.onQueryChange(spoken)
            viewModel.commitQuery(spoken)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        SearchField(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onClear = viewModel::clearQuery,
            onSubmit = {
                viewModel.commitQuery()
                keyboard?.hide()
            },
            onVoiceSearch = {
                runCatching {
                    voiceLauncher.launch(buildVoiceIntent())
                }
            },
        )

        if (query.isNotBlank()) {
            SearchFilters(selected = filter, onSelect = viewModel::setFilter)
        }

        val bottom = contentPadding.calculateBottomPadding() + 16.dp

        when {
            state.isSearching && state.results.isEmpty -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            query.isBlank() -> RecentSearches(
                recents = recents,
                onSelect = {
                    viewModel.onQueryChange(it)
                    viewModel.commitQuery(it)
                },
                onClearAll = viewModel::clearRecentSearches,
                contentPadding = PaddingValues(bottom = bottom),
            )

            state.hasSearched && state.results.isEmpty -> NoResultsState(query = query)

            else -> ResultsList(
                state = state,
                filter = filter,
                contentPadding = PaddingValues(bottom = bottom),
                onPlaySong = { index ->
                    viewModel.commitQuery()
                    playerViewModel.play(state.results.songs, index)
                },
                onOpenAlbum = onOpenAlbum,
                onOpenArtist = onOpenArtist,
                onOpenPlaylist = onOpenPlaylist,
                onOpenFolder = onOpenFolder,
            )
        }
    }
}

private fun buildVoiceIntent() = android.content.Intent(
    android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH,
).apply {
    putExtra(
        android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
    )
    putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Say a song, artist or album")
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onVoiceSearch: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(50))
            .glassSurface(shape = RoundedCornerShape(50), alpha = 0.6f),
        placeholder = { Text("Songs, artists, albums, folders") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                    }
                }
                IconButton(onClick = onVoiceSearch) {
                    Icon(Icons.Rounded.Mic, contentDescription = "Voice search")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun SearchFilters(selected: SearchFilter, onSelect: (SearchFilter) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(SearchFilter.entries.toList(), key = { it.name }) { entry ->
            val isSelected = entry == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.glassSurface(shape = RoundedCornerShape(50), alpha = 0.5f)
                        },
                    )
                    .clickable { onSelect(entry) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentSearches(
    recents: List<String>,
    onSelect: (String) -> Unit,
    onClearAll: () -> Unit,
    contentPadding: PaddingValues,
) {
    if (recents.isEmpty()) {
        AuroraEmptyState(
            icon = Icons.Rounded.Search,
            title = "Search your music",
            message = "Find songs, albums, artists, playlists and folders.",
        )
        return
    }

    LazyColumn(contentPadding = contentPadding) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent searches",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearAll) { Text("Clear") }
            }
        }
        items(recents, key = { it }) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(entry) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(14.dp))
                Text(entry, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ResultsList(
    state: SearchUiState,
    filter: SearchFilter,
    contentPadding: PaddingValues,
    onPlaySong: (Int) -> Unit,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onOpenFolder: (String) -> Unit,
) {
    val results = state.results
    val show = { f: SearchFilter -> filter == SearchFilter.ALL || filter == f }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        if (show(SearchFilter.SONGS) && results.songs.isNotEmpty()) {
            item(key = "songsHeader") { ResultHeader("Songs", results.songs.size) }
            itemsIndexed(
                items = results.songs,
                key = { _, song -> "song_${song.id}" },
            ) { index, song ->
                SongRow(item = song, onClick = { onPlaySong(index) })
            }
        }
        if (show(SearchFilter.ALBUMS) && results.albums.isNotEmpty()) {
            item(key = "albumsHeader") { ResultHeader("Albums", results.albums.size) }
            items(results.albums, key = { "album_${it.id}" }) { album ->
                ResultRow(
                    title = album.title,
                    subtitle = album.artist,
                    artworkUri = album.artworkUri,
                    icon = Icons.Rounded.Album,
                    onClick = { onOpenAlbum(album.id) },
                )
            }
        }
        if (show(SearchFilter.ARTISTS) && results.artists.isNotEmpty()) {
            item(key = "artistsHeader") { ResultHeader("Artists", results.artists.size) }
            items(results.artists, key = { "artist_${it.id}" }) { artist ->
                ResultRow(
                    title = artist.name,
                    subtitle = formatTrackCount(artist.trackCount),
                    artworkUri = artist.artworkUri,
                    icon = Icons.Rounded.Person,
                    shape = CircleShape,
                    onClick = { onOpenArtist(artist.id) },
                )
            }
        }
        if (show(SearchFilter.PLAYLISTS) && results.playlists.isNotEmpty()) {
            item(key = "playlistsHeader") { ResultHeader("Playlists", results.playlists.size) }
            items(results.playlists, key = { "playlist_${it.id}" }) { playlist ->
                ResultRow(
                    title = playlist.name,
                    subtitle = formatTrackCount(playlist.trackCount),
                    artworkUri = playlist.artworkUris.firstOrNull(),
                    icon = Icons.Rounded.QueueMusic,
                    onClick = { onOpenPlaylist(playlist.id) },
                )
            }
        }
        if (show(SearchFilter.FOLDERS) && results.folders.isNotEmpty()) {
            item(key = "foldersHeader") { ResultHeader("Folders", results.folders.size) }
            items(results.folders, key = { "folder_${it.path}" }) { folder ->
                ResultRow(
                    title = folder.name,
                    subtitle = formatTrackCount(folder.trackCount),
                    artworkUri = folder.artworkUri,
                    icon = Icons.Rounded.QueueMusic,
                    onClick = { onOpenFolder(folder.path) },
                )
            }
        }
    }
}

@Composable
private fun ResultHeader(title: String, count: Int) {
    Text(
        text = "$title · $count",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun ResultRow(
    title: String,
    subtitle: String,
    artworkUri: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (artworkUri != null) {
            Artwork(
                uri = artworkUri,
                contentDescription = title,
                shape = shape,
                modifier = Modifier.size(48.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape)
                    .glassSurface(shape = shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
