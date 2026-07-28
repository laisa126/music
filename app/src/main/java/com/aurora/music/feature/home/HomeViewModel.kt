package com.aurora.music.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.core.common.GreetingSlot
import com.aurora.music.core.common.greetingSlot
import com.aurora.music.domain.model.Album
import com.aurora.music.domain.model.Artist
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.Mood
import com.aurora.music.domain.model.Playlist
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.ScanState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val greeting: GreetingSlot = GreetingSlot.MORNING,
    val continueListening: List<MediaItem> = emptyList(),
    val recentlyPlayed: List<MediaItem> = emptyList(),
    val recentlyAdded: List<MediaItem> = emptyList(),
    val favouriteSongs: List<MediaItem> = emptyList(),
    val favouriteAlbums: List<Album> = emptyList(),
    val favouriteArtists: List<Artist> = emptyList(),
    val pinnedPlaylists: List<Playlist> = emptyList(),
    val mostPlayed: List<MediaItem> = emptyList(),
    val randomPicks: List<MediaItem> = emptyList(),
    val recommended: List<MediaItem> = emptyList(),
    val moods: List<Mood> = Mood.entries,
    val scanState: ScanState = ScanState.Idle,
    val hasLibrary: Boolean = true,
    val isLoading: Boolean = true,
) {
    val isEmptyLibrary: Boolean get() = !isLoading && !hasLibrary
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeSongs(),
        repository.observeRecentlyPlayed(20),
        repository.observeRecentlyAdded(20),
        repository.observeFavouriteSongs(),
        combine(
            repository.observeFavouriteAlbums(),
            repository.observeFavouriteArtists(),
            repository.observePlaylists(),
            repository.observeMostPlayed(20),
            repository.scanState,
        ) { albums, artists, playlists, mostPlayed, scan ->
            Extras(albums, artists, playlists, mostPlayed, scan)
        },
    ) { songs, recentlyPlayed, recentlyAdded, favourites, extras ->
        HomeUiState(
            greeting = greetingSlot(),
            // "Continue Listening" = partially-played recent tracks.
            continueListening = recentlyPlayed.take(8),
            recentlyPlayed = recentlyPlayed,
            recentlyAdded = recentlyAdded,
            favouriteSongs = favourites.take(20),
            favouriteAlbums = extras.albums.take(20),
            favouriteArtists = extras.artists.take(20),
            pinnedPlaylists = extras.playlists.filter { it.isPinned },
            mostPlayed = extras.mostPlayed,
            randomPicks = songs.shuffled().take(12),
            recommended = buildRecommendations(songs, extras.mostPlayed, favourites),
            scanState = extras.scan,
            hasLibrary = songs.isNotEmpty(),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private data class Extras(
        val albums: List<Album>,
        val artists: List<Artist>,
        val playlists: List<Playlist>,
        val mostPlayed: List<MediaItem>,
        val scan: ScanState,
    )

    /**
     * Local-only recommendations: prefer unplayed tracks by artists the user
     * already listens to. No cloud, no profiling, no account.
     */
    private fun buildRecommendations(
        all: List<MediaItem>,
        mostPlayed: List<MediaItem>,
        favourites: List<MediaItem>,
    ): List<MediaItem> {
        if (all.isEmpty()) return emptyList()
        val affinity = (mostPlayed + favourites).map { it.artistId }.toSet()
        val heard = (mostPlayed + favourites).map { it.id }.toSet()
        return all.asSequence()
            .filter { it.artistId in affinity && it.id !in heard }
            .sortedByDescending { it.dateAddedEpochSeconds }
            .take(12)
            .toList()
            .ifEmpty { all.shuffled().take(12) }
    }

    fun scanLibrary() {
        viewModelScope.launch { repository.rescan(force = true) }
    }

    fun songsForMood(mood: Mood, all: List<MediaItem>): List<MediaItem> {
        val matches = all.filter { song ->
            val genre = song.genre?.lowercase().orEmpty()
            mood.keywords.any { genre.contains(it) }
        }
        return matches.ifEmpty { all.shuffled().take(30) }
    }
}
