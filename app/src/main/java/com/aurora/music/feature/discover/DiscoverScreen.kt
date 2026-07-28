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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.aurora.music.domain.model.SmartPlaylist
import com.aurora.music.feature.player.PlayerViewModel

/**
 * Local-library discovery in Phase 1. Sections are data-source agnostic so
 * catalog-sourced rows can be appended in Phase 2 without touching this file.
 */
@Composable
fun DiscoverScreen(
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (Long) -> Unit,
    onOpenGenre: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onSeeAll: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: DiscoverViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item(key = "title") {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp),
            )
        }

        item(key = "categories") {
            CategoryGrid(
                onCategoryClick = onSeeAll,
                genreCount = state.genres.size,
                folderCount = state.folders.size,
            )
        }

        if (state.hiddenGems.isNotEmpty()) {
            item(key = "gemsHeader") {
                SectionHeader(
                    title = "Hidden Gems",
                    subtitle = "Loved but rarely played",
                    onSeeAll = { onSeeAll("smart:${SmartPlaylist.HIDDEN_GEMS.name}") },
                )
            }
            item(key = "gems") {
                SongCarousel(state.hiddenGems) { playerViewModel.play(state.hiddenGems, it) }
            }
        }

        if (state.lossless.isNotEmpty()) {
            item(key = "losslessHeader") {
                SectionHeader(
                    title = "Lossless Collection",
                    subtitle = "FLAC, ALAC, WAV and more",
                    onSeeAll = { onSeeAll("smart:${SmartPlaylist.FLAC_COLLECTION.name}") },
                )
            }
            item(key = "lossless") {
                SongCarousel(state.lossless) { playerViewModel.play(state.lossless, it) }
            }
        }

        if (state.highestQuality.isNotEmpty()) {
            item(key = "hqHeader") {
                SectionHeader(
                    title = "Highest Quality Audio",
                    onSeeAll = { onSeeAll("smart:${SmartPlaylist.HIGH_QUALITY.name}") },
                )
            }
            item(key = "hq") {
                SongCarousel(state.highestQuality) {
                    playerViewModel.play(state.highestQuality, it)
                }
            }
        }

        if (state.recentlyImported.isNotEmpty()) {
            item(key = "importedHeader") {
                SectionHeader(
                    title = "Recently Imported",
                    onSeeAll = { onSeeAll("smart:${SmartPlaylist.RECENTLY_ADDED.name}") },
                )
            }
            item(key = "imported") {
                SongCarousel(state.recentlyImported) {
                    playerViewModel.play(state.recentlyImported, it)
                }
            }
        }

        if (state.albums.isNotEmpty()) {
            item(key = "albumsHeader") {
                SectionHeader(title = "Albums", onSeeAll = { onSeeAll("albums") })
            }
            item(key = "albums") {
                AlbumCarousel(state.albums.take(20)) { onOpenAlbum(it.id) }
            }
        }

        if (state.artists.isNotEmpty()) {
            item(key = "artistsHeader") {
                SectionHeader(title = "Artists", onSeeAll = { onSeeAll("artists") })
            }
            item(key = "artists") {
                ArtistCarousel(state.artists.take(20)) { onOpenArtist(it.id) }
            }
        }

        if (state.genres.isNotEmpty()) {
            item(key = "genresHeader") { SectionHeader(title = "Genres") }
            item(key = "genres") {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.genres, key = { it.id }) { genre ->
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .glassSurface(shape = RoundedCornerShape(16.dp))
                                .clickable { onOpenGenre(genre.name) }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Column {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${genre.trackCount} tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "spacer") { Spacer(Modifier.height(24.dp)) }
    }
}

private data class DiscoverCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
)

@Composable
private fun CategoryGrid(
    onCategoryClick: (String) -> Unit,
    genreCount: Int,
    folderCount: Int,
) {
    val categories = listOf(
        DiscoverCategory("genres", "Genres", Icons.Rounded.Category),
        DiscoverCategory("artists", "Artists", Icons.Rounded.Person),
        DiscoverCategory("albums", "Albums", Icons.Rounded.Album),
        DiscoverCategory("years", "Years", Icons.Rounded.CalendarMonth),
        DiscoverCategory("folders", "Folders", Icons.Rounded.Folder),
        DiscoverCategory("smart:RECENTLY_ADDED", "Recently Imported", Icons.Rounded.NewReleases),
        DiscoverCategory("smart:MOST_PLAYED", "Highest Rated", Icons.Rounded.Star),
        DiscoverCategory("smart:LONGEST", "Longest Songs", Icons.Rounded.Timelapse),
        DiscoverCategory("smart:SHORTEST", "Shortest Songs", Icons.Rounded.AccessTime),
        DiscoverCategory("smart:HIGH_QUALITY", "Highest Quality", Icons.Rounded.HighQuality),
        DiscoverCategory("smart:FLAC_COLLECTION", "Lossless", Icons.Rounded.GraphicEq),
        DiscoverCategory("smart:HIDDEN_GEMS", "Hidden Gems", Icons.Rounded.Diamond),
        DiscoverCategory("smartPlaylists", "Smart Playlists", Icons.Rounded.AutoAwesome),
    )

    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .glassSurface(shape = RoundedCornerShape(18.dp))
                    .clickable { onCategoryClick(category.id) }
                    .padding(14.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
