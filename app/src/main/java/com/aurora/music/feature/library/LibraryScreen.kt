package com.aurora.music.feature.library

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDurationLong
import com.aurora.music.core.common.formatFileSize
import com.aurora.music.core.common.formatTrackCount
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.EmptyLibraryState
import com.aurora.music.core.designsystem.components.MediaCard
import com.aurora.music.core.designsystem.components.ScanProgressState
import com.aurora.music.core.designsystem.components.SongRow
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.repository.ScanState
import com.aurora.music.domain.repository.SortOrder
import com.aurora.music.feature.player.PlayerViewModel

@Composable
fun LibraryScreen(
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenGenre: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()
    val currentId = playerState.current?.id

    var showCreatePlaylist by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = contentPadding.calculateTopPadding() + 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Rounded.Sort, contentDescription = "Sort")
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label()) },
                            onClick = {
                                viewModel.setSortOrder(order)
                                showSortMenu = false
                            },
                        )
                    }
                }
            }
            IconButton(onClick = { showCreatePlaylist = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Create playlist")
            }
        }

        LibraryTabs(selected = tab, onSelect = viewModel::selectTab)

        val scanState = state.scanState
        if (scanState is ScanState.Scanning) {
            ScanProgressState(scanned = scanState.scanned, total = scanState.total)
        }

        if (state.isEmpty && scanState !is ScanState.Scanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyLibraryState(onScan = viewModel::rescan)
            }
            return@Column
        }

        val bottomPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)

        when (tab) {
            LibraryTab.SONGS -> SongList(
                songs = state.songs,
                currentId = currentId,
                contentPadding = bottomPadding,
                onPlay = { index -> playerViewModel.play(state.songs, index) },
                onFavourite = viewModel::toggleFavourite,
            )
            LibraryTab.FAVOURITES -> SongList(
                songs = state.favourites,
                currentId = currentId,
                contentPadding = bottomPadding,
                onPlay = { index -> playerViewModel.play(state.favourites, index) },
                onFavourite = viewModel::toggleFavourite,
            )
            LibraryTab.ALBUMS -> LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(state.albums, key = { it.id }) { album ->
                    MediaCard(
                        title = album.title,
                        subtitle = album.artist,
                        artworkUri = album.artworkUri,
                        onClick = { onOpenAlbum(album.id) },
                    )
                }
            }
            LibraryTab.ARTISTS -> LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(state.artists, key = { it.id }) { artist ->
                    MediaCard(
                        title = artist.name,
                        subtitle = formatTrackCount(artist.trackCount),
                        artworkUri = artist.artworkUri,
                        shape = CircleShape,
                        onClick = { onOpenArtist(artist.id) },
                    )
                }
            }
            LibraryTab.GENRES -> LazyColumn(contentPadding = bottomPadding) {
                items(state.genres, key = { it.id }) { genre ->
                    SimpleRow(
                        title = genre.name,
                        subtitle = "${formatTrackCount(genre.trackCount)} · " +
                            "${genre.albumCount} albums",
                        artworkUri = genre.artworkUri,
                        onClick = { onOpenGenre(genre.name) },
                    )
                }
            }
            LibraryTab.FOLDERS -> LazyColumn(contentPadding = bottomPadding) {
                items(state.folders, key = { it.path }) { folder ->
                    SimpleRow(
                        title = folder.name,
                        subtitle = "${formatTrackCount(folder.trackCount)} · " +
                            formatFileSize(folder.totalSizeBytes),
                        artworkUri = folder.artworkUri,
                        icon = Icons.Rounded.Folder,
                        onClick = { onOpenFolder(folder.path) },
                    )
                }
            }
            LibraryTab.PLAYLISTS -> LazyColumn(contentPadding = bottomPadding) {
                items(state.playlists, key = { it.id }) { playlist ->
                    SimpleRow(
                        title = playlist.name,
                        subtitle = "${formatTrackCount(playlist.trackCount)} · " +
                            formatDurationLong(playlist.totalDurationMs),
                        artworkUri = playlist.artworkUris.firstOrNull(),
                        icon = Icons.Rounded.QueueMusic,
                        trailingIcon = if (playlist.isPinned) Icons.Rounded.PushPin else null,
                        onClick = { onOpenPlaylist(playlist.id) },
                    )
                }
                if (state.playlists.isEmpty()) {
                    item {
                        Text(
                            text = "No playlists yet. Tap + to create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            }
        }
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylist = false
            },
        )
    }
}

@Composable
private fun LibraryTabs(selected: LibraryTab, onSelect: (LibraryTab) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(LibraryTab.entries.toList(), key = { it.route }) { tab ->
            val isSelected = tab == selected
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
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelLarge,
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
private fun SongList(
    songs: List<MediaItem>,
    currentId: String?,
    contentPadding: PaddingValues,
    onPlay: (Int) -> Unit,
    onFavourite: (MediaItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = 8.dp,
            bottom = contentPadding.calculateBottomPadding(),
        ),
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                item = song,
                isPlaying = song.id == currentId,
                onClick = { onPlay(index) },
                onLongClick = { onFavourite(song) },
            )
        }
    }
}

@Composable
private fun SimpleRow(
    title: String,
    subtitle: String,
    artworkUri: String?,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(52.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .glassSurface(shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon ?: Icons.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
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
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "New playlist" }) },
                enabled = name.isNotBlank(),
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun SortOrder.label(): String = when (this) {
    SortOrder.TITLE -> "Title"
    SortOrder.ARTIST -> "Artist"
    SortOrder.ALBUM -> "Album"
    SortOrder.DATE_ADDED -> "Recently added"
    SortOrder.DATE_PLAYED -> "Recently played"
    SortOrder.PLAY_COUNT -> "Most played"
    SortOrder.DURATION -> "Duration"
}
