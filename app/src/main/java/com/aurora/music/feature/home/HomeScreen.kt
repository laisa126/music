package com.aurora.music.feature.home

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.R
import com.aurora.music.core.common.GreetingSlot
import com.aurora.music.core.designsystem.components.AlbumCarousel
import com.aurora.music.core.designsystem.components.ArtistCarousel
import com.aurora.music.core.designsystem.components.EmptyLibraryState
import com.aurora.music.core.designsystem.components.ScanProgressState
import com.aurora.music.core.designsystem.components.SectionHeader
import com.aurora.music.core.designsystem.components.SongCarousel
import com.aurora.music.core.designsystem.contourGlow
import com.aurora.music.core.designsystem.montage.MontageChip
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.Mood
import com.aurora.music.domain.repository.ScanState
import com.aurora.music.feature.player.PlayerViewModel

@Composable
fun HomeScreen(
    onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit,
    onOpenEqualizer: () -> Unit, onOpenSleepTimer: () -> Unit,
    onSeeAll: (String) -> Unit, contentPadding: PaddingValues,
    modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state = state, contentPadding = contentPadding, onScan = viewModel::scanLibrary, onPlay = { items, index -> playerViewModel.play(items, index) }, onShuffleAll = { playerViewModel.shuffleAll(state.recentlyAdded + state.recentlyPlayed) }, onResume = playerViewModel::togglePlayPause, onOpenAlbum = onOpenAlbum, onOpenArtist = onOpenArtist, onOpenEqualizer = onOpenEqualizer, onOpenSleepTimer = onOpenSleepTimer, onSeeAll = onSeeAll, modifier = modifier)
}

@Composable
private fun HomeContent(
    state: HomeUiState, contentPadding: PaddingValues, onScan: () -> Unit,
    onPlay: (List<MediaItem>, Int) -> Unit, onShuffleAll: () -> Unit,
    onResume: () -> Unit, onOpenAlbum: (Long) -> Unit, onOpenArtist: (Long) -> Unit,
    onOpenEqualizer: () -> Unit, onOpenSleepTimer: () -> Unit,
    onSeeAll: (String) -> Unit, modifier: Modifier = Modifier,
) {
    val scanState = state.scanState
    val titles = SectionTitles(continueListening = stringResource(R.string.section_continue_listening), recentlyPlayed = stringResource(R.string.section_recently_played), recentlyAdded = stringResource(R.string.section_recently_added), favouriteSongs = stringResource(R.string.section_favourite_songs), favouriteAlbums = stringResource(R.string.section_favourite_albums), favouriteArtists = stringResource(R.string.section_favourite_artists), mostPlayed = stringResource(R.string.section_most_played), recommended = stringResource(R.string.section_recommended), randomPicks = stringResource(R.string.section_random_picks), moods = stringResource(R.string.section_moods))
    if (state.isEmptyLibrary && scanState !is ScanState.Scanning) { Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EmptyLibraryState(onScan = onScan) }; return }
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = contentPadding) {
        item(key = "greeting") { GreetingHeader(state.greeting) }
        if (scanState is ScanState.Scanning) { item(key = "scan") { ScanProgressState(scanned = scanState.scanned, total = scanState.total) } }
        item(key = "quickActions") { QuickActions(onShuffleAll = onShuffleAll, onResume = onResume, onScan = onScan, onEqualizer = onOpenEqualizer, onSleepTimer = onOpenSleepTimer) }
        songSection(key = "continue", title = titles.continueListening, items = state.continueListening, onPlay = onPlay, onSeeAll = { onSeeAll("continue") })
        songSection(key = "recentlyPlayed", title = titles.recentlyPlayed, items = state.recentlyPlayed, onPlay = onPlay, onSeeAll = { onSeeAll("recentlyPlayed") })
        songSection(key = "recentlyAdded", title = titles.recentlyAdded, items = state.recentlyAdded, onPlay = onPlay, onSeeAll = { onSeeAll("recentlyAdded") })
        songSection(key = "favourites", title = titles.favouriteSongs, items = state.favouriteSongs, onPlay = onPlay, onSeeAll = { onSeeAll("favourites") })
        if (state.favouriteAlbums.isNotEmpty()) { item(key = "favAlbumsHeader") { SectionHeader(title = titles.favouriteAlbums, onSeeAll = { onSeeAll("favouriteAlbums") }) }; item(key = "favAlbums") { AlbumCarousel(albums = state.favouriteAlbums, onAlbumClick = { album -> onOpenAlbum(album.id) }) } }
        if (state.favouriteArtists.isNotEmpty()) { item(key = "favArtistsHeader") { SectionHeader(title = titles.favouriteArtists, onSeeAll = { onSeeAll("favouriteArtists") }) }; item(key = "favArtists") { ArtistCarousel(artists = state.favouriteArtists, onArtistClick = { artist -> onOpenArtist(artist.id) }) } }
        songSection(key = "mostPlayed", title = titles.mostPlayed, items = state.mostPlayed, onPlay = onPlay, onSeeAll = { onSeeAll("mostPlayed") })
        songSection(key = "recommended", title = titles.recommended, items = state.recommended, onPlay = onPlay)
        songSection(key = "random", title = titles.randomPicks, items = state.randomPicks, onPlay = onPlay)
        item(key = "moodsHeader") { SectionHeader(title = titles.moods) }
        item(key = "moods") { MoodRow(moods = state.moods, onMoodClick = { onSeeAll("mood:${it.name}") }) }
        item(key = "bottomSpacer") { Spacer(Modifier.height(MontageSpacing.xxl)) }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.songSection(key: String, title: String, items: List<MediaItem>, onPlay: (List<MediaItem>, Int) -> Unit, onSeeAll: (() -> Unit)? = null) {
    if (items.isEmpty()) return
    item(key = "${key}_header") { SectionHeader(title = title, onSeeAll = onSeeAll) }
    item(key = "${key}_row") { SongCarousel(items = items, onItemClick = { index -> onPlay(items, index) }) }
}

@Composable
private fun GreetingHeader(slot: GreetingSlot) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    val text = when (slot) { GreetingSlot.MORNING -> stringResource(R.string.greeting_morning); GreetingSlot.AFTERNOON -> stringResource(R.string.greeting_afternoon); GreetingSlot.EVENING -> stringResource(R.string.greeting_evening) }
    Column(modifier = Modifier.padding(start = MontageSpacing.screenHorizontal, end = MontageSpacing.screenHorizontal, top = MontageSpacing.lg)) {
        MontageText(text = text, style = typography.title, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        MontageText(text = "What would you like to hear?", style = typography.caption, color = colors.textSecondary)
    }
}

@Composable
private fun QuickActions(onShuffleAll: () -> Unit, onResume: () -> Unit, onScan: () -> Unit, onEqualizer: () -> Unit, onSleepTimer: () -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(top = MontageSpacing.lg), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.sm)) {
        item { MontageChip(label = stringResource(R.string.action_shuffle_all), selected = false, onClick = onShuffleAll) }
        item { MontageChip(label = stringResource(R.string.action_resume), selected = false, onClick = onResume) }
        item { MontageChip(label = stringResource(R.string.action_scan_device), selected = false, onClick = onScan) }
        item { MontageChip(label = stringResource(R.string.action_equalizer), selected = false, onClick = onEqualizer) }
        item { MontageChip(label = stringResource(R.string.action_sleep_timer), selected = false, onClick = onSleepTimer) }
    }
}

@Composable
private fun MoodRow(moods: List<Mood>, onMoodClick: (Mood) -> Unit) {
    val typography = MontageTheme.typography
    LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
        items(moods, key = { it.name }) { mood ->
            val mColors = moodColors(mood)
            Box(modifier = Modifier.size(width = 150.dp, height = 84.dp).clip(RoundedCornerShape(com.aurora.music.core.designsystem.montage.MontageShapes.card)).background(Brush.linearGradient(mColors)).contourGlow(shape = RoundedCornerShape(com.aurora.music.core.designsystem.montage.MontageShapes.card), intensity = 0.6f).clickable { onMoodClick(mood) }.padding(14.dp), contentAlignment = Alignment.BottomStart) {
                MontageText(text = mood.title, style = typography.caption, color = androidx.compose.ui.graphics.Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun moodColors(mood: Mood): List<androidx.compose.ui.graphics.Color> = when (mood) {
    Mood.RELAX -> listOf(androidx.compose.ui.graphics.Color(0xFF2E7D8F), androidx.compose.ui.graphics.Color(0xFF1B4B5A))
    Mood.WORKOUT -> listOf(androidx.compose.ui.graphics.Color(0xFFD9484B), androidx.compose.ui.graphics.Color(0xFF7A1F2B))
    Mood.FOCUS -> listOf(androidx.compose.ui.graphics.Color(0xFF4B5FD9), androidx.compose.ui.graphics.Color(0xFF232B6B))
    Mood.SLEEP -> listOf(androidx.compose.ui.graphics.Color(0xFF3B3269), androidx.compose.ui.graphics.Color(0xFF171432))
    Mood.TRAVEL -> listOf(androidx.compose.ui.graphics.Color(0xFF2E8F6B), androidx.compose.ui.graphics.Color(0xFF14503B))
    Mood.PARTY -> listOf(androidx.compose.ui.graphics.Color(0xFFD94BA8), androidx.compose.ui.graphics.Color(0xFF6B1F55))
    Mood.ROMANCE -> listOf(androidx.compose.ui.graphics.Color(0xFFD9744B), androidx.compose.ui.graphics.Color(0xFF6B2E1F))
}

private data class SectionTitles(val continueListening: String, val recentlyPlayed: String, val recentlyAdded: String, val favouriteSongs: String, val favouriteAlbums: String, val favouriteArtists: String, val mostPlayed: String, val recommended: String, val randomPicks: String, val moods: String)
