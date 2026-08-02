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
import com.aurora.music.core.designsystem.montage.MontageChip
import com.aurora.music.core.designsystem.montage.MontageDialog
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageSecondaryButton
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageTextField
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.repository.ScanState
import com.aurora.music.domain.repository.SortOrder
import com.aurora.music.feature.player.PlayerViewModel

@Composable
fun LibraryScreen(
    onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit,
    onOpenGenre: (String) -> Unit, onOpenFolder: (String) -> Unit,
    onOpenPlaylist: (Long) -> Unit, contentPadding: PaddingValues,
    modifier: Modifier = Modifier, viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()
    val currentId = playerState.current?.id
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val colors = MontageTheme.colors; val typography = MontageTheme.typography

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = MontageSpacing.screenHorizontal, end = MontageSpacing.sm, top = contentPadding.calculateTopPadding() + MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            MontageText(text = "Library", style = typography.title, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.weight(1f))
            MontageIconButton(onClick = { showSortMenu = true }) { MontageIcon(Icons.Rounded.Sort, contentDescription = "Sort", tint = colors.textSecondary) }
            MontageIconButton(onClick = { showCreatePlaylist = true }) { MontageIcon(Icons.Rounded.Add, contentDescription = "Create playlist", tint = colors.textSecondary) }
        }
        LibraryTabs(selected = tab, onSelect = viewModel::selectTab)
        val scanState = state.scanState
        if (scanState is ScanState.Scanning) { ScanProgressState(scanned = scanState.scanned, total = scanState.total) }
        if (state.isEmpty && scanState !is ScanState.Scanning) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EmptyLibraryState(onScan = viewModel::rescan) }; return@Column }
        val bottomPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp)
        when (tab) {
            LibraryTab.SONGS -> SongList(songs = state.songs, currentId = currentId, contentPadding = bottomPadding, onPlay = { index -> playerViewModel.play(state.songs, index) }, onFavourite = viewModel::toggleFavourite)
            LibraryTab.FAVOURITES -> SongList(songs = state.favourites, currentId = currentId, contentPadding = bottomPadding, onPlay = { index -> playerViewModel.play(state.favourites, index) }, onFavourite = viewModel::toggleFavourite)
            LibraryTab.ALBUMS -> LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), contentPadding = PaddingValues(start = MontageSpacing.md, end = MontageSpacing.md, top = MontageSpacing.sm, bottom = contentPadding.calculateBottomPadding() + 16.dp), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md), verticalArrangement = Arrangement.spacedBy(MontageSpacing.lg)) { items(state.albums, key = { it.id }) { album -> MediaCard(title = album.title, subtitle = album.artist, artworkUri = album.artworkUri, onClick = { onOpenAlbum(album.id) }) } }
            LibraryTab.ARTISTS -> LazyVerticalGrid(columns = GridCells.Adaptive(140.dp), contentPadding = PaddingValues(start = MontageSpacing.md, end = MontageSpacing.md, top = MontageSpacing.sm, bottom = contentPadding.calculateBottomPadding() + 16.dp), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md), verticalArrangement = Arrangement.spacedBy(MontageSpacing.lg)) { items(state.artists, key = { it.id }) { artist -> MediaCard(title = artist.name, subtitle = formatTrackCount(artist.trackCount), artworkUri = artist.artworkUri, shape = CircleShape, onClick = { onOpenArtist(artist.id) }) } }
            LibraryTab.GENRES -> LazyColumn(contentPadding = bottomPadding) { items(state.genres, key = { it.id }) { genre -> SimpleRow(title = genre.name, subtitle = "${formatTrackCount(genre.trackCount)} · ${genre.albumCount} albums", artworkUri = genre.artworkUri, onClick = { onOpenGenre(genre.name) }) } }
            LibraryTab.FOLDERS -> LazyColumn(contentPadding = bottomPadding) { items(state.folders, key = { it.path }) { folder -> SimpleRow(title = folder.name, subtitle = "${formatTrackCount(folder.trackCount)} · ${formatFileSize(folder.totalSizeBytes)}", artworkUri = folder.artworkUri, icon = Icons.Rounded.Folder, onClick = { onOpenFolder(folder.path) }) } }
            LibraryTab.PLAYLISTS -> LazyColumn(contentPadding = bottomPadding) { items(state.playlists, key = { it.id }) { playlist -> SimpleRow(title = playlist.name, subtitle = "${formatTrackCount(playlist.trackCount)} · ${formatDurationLong(playlist.totalDurationMs)}", artworkUri = playlist.artworkUris.firstOrNull(), icon = Icons.Rounded.QueueMusic, trailingIcon = if (playlist.isPinned) Icons.Rounded.PushPin else null, onClick = { onOpenPlaylist(playlist.id) }) }; if (state.playlists.isEmpty()) { item { MontageText(text = "No playlists yet. Tap + to create one.", style = typography.caption, color = colors.textSecondary, modifier = Modifier.padding(MontageSpacing.xxl)) } } }
        }
    }
    if (showSortMenu) { MontageDialog(onDismissRequest = { showSortMenu = false }, title = "Sort by") { Column { SortOrder.entries.forEach { order -> Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(MontageShapes.small)).clickable { viewModel.setSortOrder(order); showSortMenu = false }.padding(horizontal = MontageSpacing.lg, vertical = MontageSpacing.md)) { MontageText(text = order.label(), style = typography.body, color = colors.textPrimary) } } } } }
    if (showCreatePlaylist) { CreatePlaylistDialog(onDismiss = { showCreatePlaylist = false }, onConfirm = { name -> viewModel.createPlaylist(name); showCreatePlaylist = false }) }
}

@Composable
private fun LibraryTabs(selected: LibraryTab, onSelect: (LibraryTab) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = MontageSpacing.sm, bottom = MontageSpacing.xs), contentPadding = PaddingValues(horizontal = MontageSpacing.md), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm)) {
        items(LibraryTab.entries.toList(), key = { it.route }) { tab -> MontageChip(label = tab.title, selected = tab == selected, onClick = { onSelect(tab) }) }
    }
}

@Composable
private fun SongList(songs: List<MediaItem>, currentId: String?, contentPadding: PaddingValues, onPlay: (Int) -> Unit, onFavourite: (MediaItem) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = MontageSpacing.sm, end = MontageSpacing.sm, top = MontageSpacing.sm, bottom = contentPadding.calculateBottomPadding())) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song -> SongRow(item = song, isPlaying = song.id == currentId, onClick = { onPlay(index) }, onLongClick = { onFavourite(song) }) }
    }
}

@Composable
private fun SimpleRow(title: String, subtitle: String, artworkUri: String?, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        if (artworkUri != null) { Artwork(uri = artworkUri, contentDescription = title, shape = RoundedCornerShape(MontageShapes.icon), modifier = Modifier.size(52.dp)) } else { Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(MontageShapes.icon)).glassSurface(shape = RoundedCornerShape(MontageShapes.icon)), contentAlignment = Alignment.Center) { MontageIcon(imageVector = icon ?: Icons.Rounded.QueueMusic, contentDescription = null, tint = colors.accent) } }
        Spacer(Modifier.width(MontageSpacing.md))
        Column(Modifier.weight(1f)) { MontageText(text = title, style = typography.caption, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis); MontageText(text = subtitle, style = typography.mini, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        if (trailingIcon != null) { MontageIcon(imageVector = trailingIcon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography; var name by remember { mutableStateOf("") }
    MontageDialog(onDismissRequest = onDismiss, title = "New playlist") {
        MontageTextField(value = name, onValueChange = { name = it }, placeholder = "Playlist name", singleLine = true)
        Spacer(Modifier.height(MontageSpacing.lg))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            MontageSecondaryButton(onClick = onDismiss) { MontageText("Cancel", style = typography.label, color = colors.textSecondary) }
            Spacer(Modifier.width(MontageSpacing.sm))
            MontagePrimaryButton(onClick = { onConfirm(name.ifBlank { "New playlist" }) }, enabled = name.isNotBlank()) { MontageText("Create", style = typography.label, color = colors.textOnAccent) }
        }
    }
}

private fun SortOrder.label(): String = when (this) { SortOrder.TITLE -> "Title"; SortOrder.ARTIST -> "Artist"; SortOrder.ALBUM -> "Album"; SortOrder.DATE_ADDED -> "Recently added"; SortOrder.DATE_PLAYED -> "Recently played"; SortOrder.PLAY_COUNT -> "Most played"; SortOrder.DURATION -> "Duration" }
