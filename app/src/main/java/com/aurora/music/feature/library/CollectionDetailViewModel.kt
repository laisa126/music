package com.aurora.music.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.SmartPlaylist
import com.aurora.music.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val title: String = "",
    val subtitle: String = "",
    val artworkUri: String? = null,
    val tracks: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Backs every "collection of tracks" screen — album, artist, genre, folder,
 * playlist and the Home/Discover "See all" grids — off the same repository
 * interface, so Phase 2 catalog collections reuse it unchanged.
 */
@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val albumId: Long? = savedStateHandle.get<String>("albumId")?.toLongOrNull()
    private val artistId: Long? = savedStateHandle.get<String>("artistId")?.toLongOrNull()
    private val playlistId: Long? = savedStateHandle.get<String>("playlistId")?.toLongOrNull()
    private val genreName: String? = savedStateHandle.get<String>("genreName")?.decode()
    private val folderPath: String? = savedStateHandle.get<String>("folderPath")?.decode()
    private val sectionId: String? = savedStateHandle.get<String>("sectionId")?.decode()

    val uiState: StateFlow<CollectionDetailUiState> = buildFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CollectionDetailUiState(),
        )

    private fun buildFlow(): Flow<CollectionDetailUiState> = when {
        albumId != null -> repository.observeAlbumTracks(albumId).map { tracks ->
            val first = tracks.firstOrNull()
            CollectionDetailUiState(
                title = first?.album ?: "Album",
                subtitle = listOfNotNull(
                    first?.albumArtist ?: first?.artist,
                    first?.year?.takeIf { it > 0 }?.toString(),
                ).joinToString(" · "),
                artworkUri = first?.artworkUri,
                tracks = tracks,
                isLoading = false,
            )
        }

        artistId != null -> repository.observeArtistTracks(artistId).map { tracks ->
            CollectionDetailUiState(
                title = tracks.firstOrNull()?.artist ?: "Artist",
                subtitle = "${tracks.map { it.albumId }.distinct().size} albums · " +
                    "${tracks.size} songs",
                artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                tracks = tracks,
                isLoading = false,
            )
        }

        playlistId != null -> repository.observePlaylistTracks(playlistId).map { tracks ->
            CollectionDetailUiState(
                title = "Playlist",
                subtitle = "${tracks.size} songs",
                artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                tracks = tracks,
                isLoading = false,
            )
        }

        genreName != null -> repository.observeGenreTracks(genreName).map { tracks ->
            CollectionDetailUiState(
                title = genreName,
                subtitle = "${tracks.size} songs",
                artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                tracks = tracks,
                isLoading = false,
            )
        }

        folderPath != null -> repository.observeFolderTracks(folderPath).map { tracks ->
            CollectionDetailUiState(
                title = folderPath.substringAfterLast('/').ifBlank { folderPath },
                subtitle = "${tracks.size} songs",
                artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                tracks = tracks,
                isLoading = false,
            )
        }

        sectionId != null -> sectionFlow(sectionId)

        else -> flowOf(CollectionDetailUiState(isLoading = false))
    }

    private fun sectionFlow(sectionId: String): Flow<CollectionDetailUiState> {
        val smart = sectionId.removePrefix("smart:")
            .let { name -> SmartPlaylist.entries.firstOrNull { it.name == name } }

        val (title, flow) = when {
            smart != null -> smart.title to repository.observeSmartPlaylist(smart, 200)
            sectionId == "recentlyAdded" ->
                "Recently Added" to repository.observeRecentlyAdded(200)
            sectionId == "recentlyPlayed" || sectionId == "continue" ->
                "Recently Played" to repository.observeRecentlyPlayed(200)
            sectionId == "mostPlayed" -> "Most Played" to repository.observeMostPlayed(200)
            sectionId == "favourites" -> "Favourite Songs" to repository.observeFavouriteSongs()
            sectionId.startsWith("mood:") -> {
                val moodName = sectionId.removePrefix("mood:")
                val mood = com.aurora.music.domain.model.Mood.entries
                    .firstOrNull { it.name == moodName }
                (mood?.title ?: "Mood") to repository.observeSongs().map { songs ->
                    if (mood == null) {
                        songs.shuffled().take(50)
                    } else {
                        songs.filter { song ->
                            val genre = song.genre?.lowercase().orEmpty()
                            mood.keywords.any { genre.contains(it) }
                        }.ifEmpty { songs.shuffled().take(50) }
                    }
                }
            }
            else -> "All Songs" to repository.observeSongs()
        }

        return flow.map { tracks ->
            CollectionDetailUiState(
                title = title,
                subtitle = "${tracks.size} songs",
                artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                tracks = tracks,
                isLoading = false,
            )
        }
    }

    fun toggleFavourite(item: MediaItem) {
        viewModelScope.launch { repository.setFavourite(item.id, !item.isFavourite) }
    }
}

private fun String.decode(): String = runCatching { URLDecoder.decode(this, "UTF-8") }
    .getOrDefault(this)
