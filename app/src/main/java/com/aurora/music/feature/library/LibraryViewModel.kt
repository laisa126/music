package com.aurora.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.domain.model.Album
import com.aurora.music.domain.model.Artist
import com.aurora.music.domain.model.Folder
import com.aurora.music.domain.model.Genre
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.Playlist
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.ScanState
import com.aurora.music.domain.repository.SettingsRepository
import com.aurora.music.domain.repository.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab(val route: String, val title: String) {
    SONGS("songs", "Songs"),
    ALBUMS("albums", "Albums"),
    ARTISTS("artists", "Artists"),
    GENRES("genres", "Genres"),
    PLAYLISTS("playlists", "Playlists"),
    FOLDERS("folders", "Folders"),
    FAVOURITES("favourites", "Favourites"),
}

data class LibraryUiState(
    val songs: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val favourites: List<MediaItem> = emptyList(),
    val scanState: ScanState = ScanState.Idle,
    val sortOrder: SortOrder = SortOrder.TITLE,
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean get() = !isLoading && songs.isEmpty()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeSongs(),
        combine(
            repository.observeAlbums(),
            repository.observeArtists(),
            repository.observeGenres(),
        ) { albums, artists, genres -> Triple(albums, artists, genres) },
        combine(
            repository.observePlaylists(),
            repository.observeFolders(),
            repository.observeFavouriteSongs(),
        ) { playlists, folders, favourites -> Triple(playlists, folders, favourites) },
        repository.scanState,
        settingsRepository.settings,
    ) { songs, collections, userLists, scan, settings ->
        LibraryUiState(
            songs = songs.sortedWith(comparatorFor(settings.sortOrder)),
            albums = collections.first,
            artists = collections.second,
            genres = collections.third,
            playlists = userLists.first,
            folders = userLists.second,
            favourites = userLists.third,
            scanState = scan,
            sortOrder = settings.sortOrder,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    private fun comparatorFor(order: SortOrder): Comparator<MediaItem> = when (order) {
        SortOrder.TITLE -> compareBy { it.title.lowercase() }
        SortOrder.ARTIST -> compareBy({ it.artist.lowercase() }, { it.title.lowercase() })
        SortOrder.ALBUM -> compareBy({ it.album.lowercase() }, { it.trackNumber })
        SortOrder.DATE_ADDED -> compareByDescending { it.dateAddedEpochSeconds }
        SortOrder.DATE_PLAYED -> compareByDescending { it.lastPlayedEpochMillis }
        SortOrder.PLAY_COUNT -> compareByDescending { it.playCount }
        SortOrder.DURATION -> compareByDescending { it.durationMs }
    }

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { settingsRepository.update { it.copy(sortOrder = order) } }
    }

    fun rescan() {
        viewModelScope.launch { repository.rescan(force = true) }
    }

    // ---- Multi-select -----------------------------------------------------

    fun toggleSelection(id: String) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun selectAll(ids: List<String>) {
        _selectedIds.value = ids.toSet()
    }

    fun invertSelection(all: List<String>) {
        val current = _selectedIds.value
        _selectedIds.value = all.filterNot { it in current }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    // ---- Track actions ----------------------------------------------------

    fun toggleFavourite(item: MediaItem) {
        viewModelScope.launch { repository.setFavourite(item.id, !item.isFavourite) }
    }

    fun hide(item: MediaItem) {
        viewModelScope.launch { repository.setHidden(item.id, true) }
    }

    fun favouriteSelected(favourite: Boolean = true) {
        val ids = _selectedIds.value
        viewModelScope.launch {
            ids.forEach { repository.setFavourite(it, favourite) }
            clearSelection()
        }
    }

    fun hideSelected() {
        val ids = _selectedIds.value
        viewModelScope.launch {
            ids.forEach { repository.setHidden(it, true) }
            clearSelection()
        }
    }

    // ---- Playlists --------------------------------------------------------

    fun createPlaylist(name: String, withSelection: Boolean = false) {
        val ids = if (withSelection) _selectedIds.value.toList() else emptyList()
        viewModelScope.launch {
            val id = repository.createPlaylist(name)
            if (ids.isNotEmpty()) repository.addToPlaylist(id, ids)
            clearSelection()
        }
    }

    fun addSelectedToPlaylist(playlistId: Long) {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.addToPlaylist(playlistId, ids)
            clearSelection()
        }
    }

    fun addToPlaylist(playlistId: Long, mediaIds: List<String>) {
        viewModelScope.launch { repository.addToPlaylist(playlistId, mediaIds) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch { repository.deletePlaylist(playlistId) }
    }

    fun renamePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch { repository.renamePlaylist(playlistId, name) }
    }

    fun togglePlaylistPinned(playlist: Playlist) {
        viewModelScope.launch {
            repository.setPlaylistPinned(playlist.id, !playlist.isPinned)
        }
    }

    suspend fun songsOf(ids: List<String>): List<MediaItem> = repository.getSongs(ids)
}
