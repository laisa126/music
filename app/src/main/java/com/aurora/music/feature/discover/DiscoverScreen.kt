package com.aurora.music.feature.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.AlbumCarousel
import com.aurora.music.core.designsystem.components.ArtistCarousel
import com.aurora.music.core.designsystem.components.SectionHeader
import com.aurora.music.core.designsystem.components.SongCarousel
import com.aurora.music.core.designsystem.glassSurface
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.domain.model.SmartPlaylist
import com.aurora.music.feature.player.PlayerViewModel

@Composable
fun DiscoverScreen(
    onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit,
    onOpenGenre: (String) -> Unit, onOpenFolder: (String) -> Unit,
    onSeeAll: (String) -> Unit, contentPadding: PaddingValues,
    modifier: Modifier = Modifier, viewModel: DiscoverViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
        item(key = "title") { MontageText(text = "Discover", style = typography.title, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.padding(start = MontageSpacing.screenHorizontal, top = MontageSpacing.lg)) }
        item(key = "categories") { CategoryGrid(onCategoryClick = onSeeAll, genreCount = state.genres.size, folderCount = state.folders.size) }
        if (state.hiddenGems.isNotEmpty()) { item(key = "gemsHeader") { SectionHeader(title = "Hidden Gems", subtitle = "Loved but rarely played", onSeeAll = { onSeeAll("smart:${SmartPlaylist.HIDDEN_GEMS.name}") }) }; item(key = "gems") { SongCarousel(items = state.hiddenGems, onItemClick = { index -> playerViewModel.play(state.hiddenGems, index) }) } }
        if (state.lossless.isNotEmpty()) { item(key = "losslessHeader") { SectionHeader(title = "Lossless Collection", subtitle = "FLAC, ALAC, WAV and more", onSeeAll = { onSeeAll("smart:${SmartPlaylist.FLAC_COLLECTION.name}") }) }; item(key = "lossless") { SongCarousel(items = state.lossless, onItemClick = { index -> playerViewModel.play(state.lossless, index) }) } }
        if (state.highestQuality.isNotEmpty()) { item(key = "hqHeader") { SectionHeader(title = "Highest Quality Audio", onSeeAll = { onSeeAll("smart:${SmartPlaylist.HIGH_QUALITY.name}") }) }; item(key = "hq") { SongCarousel(items = state.highestQuality, onItemClick = { index -> playerViewModel.play(state.highestQuality, index) }) } }
        if (state.recentlyImported.isNotEmpty()) { item(key = "importedHeader") { SectionHeader(title = "Recently Imported", onSeeAll = { onSeeAll("smart:${SmartPlaylist.RECENTLY_ADDED.name}") }) }; item(key = "imported") { SongCarousel(items = state.recentlyImported, onItemClick = { index -> playerViewModel.play(state.recentlyImported, index) }) } }
        if (state.albums.isNotEmpty()) { item(key = "albumsHeader") { SectionHeader(title = "Albums", onSeeAll = { onSeeAll("albums") }) }; item(key = "albums") { AlbumCarousel(albums = state.albums.take(20), onAlbumClick = { album -> onOpenAlbum(album.id) }) } }
        if (state.artists.isNotEmpty()) { item(key = "artistsHeader") { SectionHeader(title = "Artists", onSeeAll = { onSeeAll("artists") }) }; item(key = "artists") { ArtistCarousel(artists = state.artists.take(20), onArtistClick = { artist -> onOpenArtist(artist.id) }) } }
        if (state.genres.isNotEmpty()) { item(key = "genresHeader") { SectionHeader(title = "Genres") }; item(key = "genres") { LazyHorizontalGrid(rows = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().height(120.dp), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm), verticalArrangement = Arrangement.spacedBy(MontageSpacing.sm)) { items(state.genres, key = { it.id }) { genre -> Box(modifier = Modifier.width(150.dp).clip(RoundedCornerShape(MontageShapes.small)).glassSurface(shape = RoundedCornerShape(MontageShapes.small)).clickable { onOpenGenre(genre.name) }.padding(MontageSpacing.md), contentAlignment = Alignment.CenterStart) { Column { MontageText(text = genre.name, style = typography.caption, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis); MontageText(text = "${genre.trackCount} tracks", style = typography.mini, color = colors.textSecondary) } } } } } }
        item(key = "spacer") { Spacer(Modifier.height(MontageSpacing.xxl)) }
    }
}

private data class DiscoverCategory(val id: String, val title: String, val icon: ImageVector)

@Composable
private fun CategoryGrid(onCategoryClick: (String) -> Unit, genreCount: Int, folderCount: Int) {
    val colors = MontageTheme.colors; val typography = MontageTheme.typography
    val categories = listOf(DiscoverCategory("genres", "Genres", Icons.Rounded.Category), DiscoverCategory("artists", "Artists", Icons.Rounded.Person), DiscoverCategory("albums", "Albums", Icons.Rounded.Album), DiscoverCategory("years", "Years", Icons.Rounded.CalendarMonth), DiscoverCategory("folders", "Folders", Icons.Rounded.Folder), DiscoverCategory("smart:RECENTLY_ADDED", "Recently Imported", Icons.Rounded.NewReleases), DiscoverCategory("smart:MOST_PLAYED", "Highest Rated", Icons.Rounded.Star), DiscoverCategory("smart:LONGEST", "Longest Songs", Icons.Rounded.Timelapse), DiscoverCategory("smart:SHORTEST", "Shortest Songs", Icons.Rounded.AccessTime), DiscoverCategory("smart:HIGH_QUALITY", "Highest Quality", Icons.Rounded.HighQuality), DiscoverCategory("smart:FLAC_COLLECTION", "Lossless", Icons.Rounded.GraphicEq), DiscoverCategory("smart:HIDDEN_GEMS", "Hidden Gems", Icons.Rounded.Diamond), DiscoverCategory("smartPlaylists", "Smart Playlists", Icons.Rounded.AutoAwesome))
    LazyHorizontalGrid(rows = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = MontageSpacing.lg), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md), verticalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
        items(categories, key = { it.id }) { category -> Column(modifier = Modifier.width(120.dp).clip(RoundedCornerShape(MontageShapes.small)).glassSurface(shape = RoundedCornerShape(MontageShapes.small)).clickable { onCategoryClick(category.id) }.padding(MontageSpacing.md), verticalArrangement = Arrangement.Center) { MontageIcon(imageVector = category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(MontageIcons.large)); Spacer(Modifier.height(MontageSpacing.sm)); MontageText(text = category.title, style = typography.label, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis) } }
    }
}
