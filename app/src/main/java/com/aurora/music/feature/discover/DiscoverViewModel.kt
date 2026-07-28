package com.aurora.music.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.domain.model.Album
import com.aurora.music.domain.model.Artist
import com.aurora.music.domain.model.Folder
import com.aurora.music.domain.model.Genre
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.SmartPlaylist
import com.aurora.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DiscoverUiState(
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val hiddenGems: List<MediaItem> = emptyList(),
    val lossless: List<MediaItem> = emptyList(),
    val highestQuality: List<MediaItem> = emptyList(),
    val recentlyImported: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    repository: MusicRepository,
) : ViewModel() {

    val uiState: StateFlow<DiscoverUiState> = combine(
        combine(
            repository.observeAlbums(),
            repository.observeArtists(),
            repository.observeGenres(),
            repository.observeFolders(),
        ) { albums, artists, genres, folders ->
            listOf(albums, artists, genres, folders)
        },
        repository.observeSmartPlaylist(SmartPlaylist.HIDDEN_GEMS, 20),
        repository.observeSmartPlaylist(SmartPlaylist.FLAC_COLLECTION, 20),
        repository.observeSmartPlaylist(SmartPlaylist.HIGH_QUALITY, 20),
        repository.observeSmartPlaylist(SmartPlaylist.RECENTLY_ADDED, 20),
    ) { collections, gems, lossless, highQuality, imported ->
        @Suppress("UNCHECKED_CAST")
        DiscoverUiState(
            albums = collections[0] as List<Album>,
            artists = collections[1] as List<Artist>,
            genres = collections[2] as List<Genre>,
            folders = collections[3] as List<Folder>,
            hiddenGems = gems,
            lossless = lossless,
            highestQuality = highQuality,
            recentlyImported = imported,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoverUiState(),
    )
}
