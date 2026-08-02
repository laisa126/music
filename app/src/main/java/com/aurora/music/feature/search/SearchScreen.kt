package com.aurora.music.feature.search

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.aurora.music.core.designsystem.montage.MontageChip
import com.aurora.music.core.designsystem.montage.MontageCircularProgress
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageTextField
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.feature.player.PlayerViewModel

@Composable
fun SearchScreen(
    onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit, onOpenFolder: (String) -> Unit,
    contentPadding: PaddingValues, modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val recents by viewModel.recentSearches.collectAsStateWithLifecycle()
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val colors = MontageTheme.colors; val typography = MontageTheme.typography

    val voiceLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) { viewModel.onQueryChange(spoken); viewModel.commitQuery(spoken) }
    }

    Column(modifier = modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())) {
        MontageTextField(value = query, onValueChange = viewModel::onQueryChange, modifier = Modifier.fillMaxWidth().padding(horizontal = MontageSpacing.md, vertical = MontageSpacing.md), placeholder = "Songs, artists, albums, folders", leadingIcon = { MontageIcon(Icons.Rounded.Search, contentDescription = null, tint = colors.textSecondary) }, trailingIcon = { Row { if (query.isNotEmpty()) { MontageIconButton(onClick = viewModel::clearQuery) { MontageIcon(Icons.Rounded.Clear, contentDescription = "Clear", tint = colors.textSecondary) } }; MontageIconButton(onClick = { runCatching { voiceLauncher.launch(buildVoiceIntent()) } }) { MontageIcon(Icons.Rounded.Mic, contentDescription = "Voice search", tint = colors.textSecondary) } } }, singleLine = true)
        if (query.isNotBlank()) { SearchFilters(selected = filter, onSelect = viewModel::setFilter) }
        val bottom = contentPadding.calculateBottomPadding() + 16.dp
        when {
            state.isSearching && state.results.isEmpty -> { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { MontageCircularProgress() } }
            query.isBlank() -> RecentSearches(recents = recents, onSelect = { viewModel.onQueryChange(it); viewModel.commitQuery(it) }, onClearAll = viewModel::clearRecentSearches, contentPadding = PaddingValues(bottom = bottom))
            state.hasSearched && state.results.isEmpty -> NoResultsState(query = query)
            else -> ResultsList(state = state, filter = filter, contentPadding = PaddingValues(bottom = bottom), onPlaySong = { index -> viewModel.commitQuery(); playerViewModel.play(state.results.songs, index) }, onOpenAlbum = onOpenAlbum, onOpenArtist = onOpenArtist, onOpenPlaylist = onOpenPlaylist, onOpenFolder = onOpenFolder)
        }
    }
}

private fun buildVoiceIntent() = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply { putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Say a song, artist or album") }

@Composable
private fun SearchFilters(selected: SearchFilter, onSelect: (SearchFilter) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MontageSpacing.md), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm)) {
        items(SearchFilter.entries.toList(), key = { it.name }) { entry -> MontageChip(label = entry.label, selected = entry == selected, onClick = { onSelect(entry) }) }
    }
}

@Composable
private fun RecentSearches(recents: List<String>, onSelect: (String) -> Unit, onClearAll: () -> Unit, contentPadding: PaddingValues) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    if (recents.isEmpty()) { AuroraEmptyState(icon = Icons.Rounded.Search, title = "Search your music", message = "Find songs, albums, artists, playlists and folders."); return }
    LazyColumn(contentPadding = contentPadding) {
        item { Row(modifier = Modifier.fillMaxWidth().padding(start = MontageSpacing.screenHorizontal, end = MontageSpacing.sm, top = MontageSpacing.sm), verticalAlignment = Alignment.CenterVertically) { MontageText(text = "Recent searches", style = typography.body, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.weight(1f)); MontageChip(label = "Clear", selected = false, onClick = onClearAll) } }
        items(recents, key = { it }) { entry -> Row(modifier = Modifier.fillMaxWidth().clickable { onSelect(entry) }.padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) { MontageIcon(Icons.Rounded.History, contentDescription = null, tint = colors.textSecondary); Spacer(Modifier.width(MontageSpacing.md)); MontageText(entry, style = typography.caption, color = colors.textPrimary) } }
    }
}

@Composable
private fun ResultsList(state: SearchUiState, filter: SearchFilter, contentPadding: PaddingValues, onPlaySong: (Int) -> Unit, onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit, onOpenPlaylist: (Long) -> Unit, onOpenFolder: (String) -> Unit) {
    val results = state.results; val show = { f: SearchFilter -> filter == SearchFilter.ALL || filter == f }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        if (show(SearchFilter.SONGS) && results.songs.isNotEmpty()) { item(key = "songsHeader") { ResultHeader("Songs", results.songs.size) }; itemsIndexed(items = results.songs, key = { _, song -> "song_${song.id}" }) { index, song -> SongRow(item = song, onClick = { onPlaySong(index) }) } }
        if (show(SearchFilter.ALBUMS) && results.albums.isNotEmpty()) { item(key = "albumsHeader") { ResultHeader("Albums", results.albums.size) }; items(results.albums, key = { "album_${it.id}" }) { album -> ResultRow(title = album.title, subtitle = album.artist, artworkUri = album.artworkUri, onClick = { onOpenAlbum(album.id) }) } }
        if (show(SearchFilter.ARTISTS) && results.artists.isNotEmpty()) { item(key = "artistsHeader") { ResultHeader("Artists", results.artists.size) }; items(results.artists, key = { "artist_${it.id}" }) { artist -> ResultRow(title = artist.name, subtitle = formatTrackCount(artist.trackCount), artworkUri = artist.artworkUri, onClick = { onOpenArtist(artist.id) }) } }
        if (show(SearchFilter.PLAYLISTS) && results.playlists.isNotEmpty()) { item(key = "playlistsHeader") { ResultHeader("Playlists", results.playlists.size) }; items(results.playlists, key = { "playlist_${it.id}" }) { playlist -> ResultRow(title = playlist.name, subtitle = formatTrackCount(playlist.trackCount), artworkUri = playlist.artworkUris.firstOrNull(), onClick = { onOpenPlaylist(playlist.id) }) } }
        if (show(SearchFilter.FOLDERS) && results.folders.isNotEmpty()) { item(key = "foldersHeader") { ResultHeader("Folders", results.folders.size) }; items(results.folders, key = { "folder_${it.path}" }) { folder -> ResultRow(title = folder.name, subtitle = formatTrackCount(folder.trackCount), artworkUri = folder.artworkUri, onClick = { onOpenFolder(folder.path) }) } }
    }
}

@Composable private fun ResultHeader(title: String, count: Int) { val colors = MontageTheme.colors; val typography = MontageTheme.typography; MontageText(text = "$title · $count", style = typography.caption, color = colors.accent, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(start = MontageSpacing.screenHorizontal, top = MontageSpacing.lg, bottom = MontageSpacing.xs)) }

@Composable
private fun ResultRow(title: String, subtitle: String, artworkUri: String?, onClick: () -> Unit) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = MontageSpacing.screenHorizontal, vertical = MontageSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        if (artworkUri != null) { Artwork(uri = artworkUri, contentDescription = title, shape = RoundedCornerShape(MontageShapes.icon), modifier = Modifier.size(48.dp)) } else { Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(MontageShapes.icon)).glassSurface(shape = RoundedCornerShape(MontageShapes.icon)), contentAlignment = Alignment.Center) { MontageIcon(Icons.Rounded.Search, contentDescription = null, tint = colors.accent) } }
        Spacer(Modifier.width(MontageSpacing.md))
        Column(Modifier.weight(1f)) { MontageText(text = title, style = typography.caption, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis); MontageText(text = subtitle, style = typography.mini, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}
