package com.aurora.music.domain.repository

import com.aurora.music.domain.model.Album
import com.aurora.music.domain.model.Artist
import com.aurora.music.domain.model.Folder
import com.aurora.music.domain.model.Genre
import com.aurora.music.domain.model.Lyrics
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.PlaybackHistoryEntry
import com.aurora.music.domain.model.Playlist
import com.aurora.music.domain.model.SmartPlaylist
import kotlinx.coroutines.flow.Flow

/**
 * The only data contract the UI/ViewModel layer knows about.
 *
 * Phase 1 ships [com.aurora.music.data.repository.LocalMusicRepository].
 * Phase 2 adds a catalog implementation plus a merged implementation; no
 * consumer of this interface needs to change (spec Section 2a).
 */
interface MusicRepository {

    // ---- Library ----------------------------------------------------------
    fun observeSongs(): Flow<List<MediaItem>>
    fun observeAlbums(): Flow<List<Album>>
    fun observeArtists(): Flow<List<Artist>>
    fun observeGenres(): Flow<List<Genre>>
    fun observeFolders(): Flow<List<Folder>>

    fun observeSong(id: String): Flow<MediaItem?>
    fun observeAlbumTracks(albumId: Long): Flow<List<MediaItem>>
    fun observeArtistTracks(artistId: Long): Flow<List<MediaItem>>
    fun observeGenreTracks(genreName: String): Flow<List<MediaItem>>
    fun observeFolderTracks(folderPath: String): Flow<List<MediaItem>>

    suspend fun getSong(id: String): MediaItem?
    suspend fun getSongs(ids: List<String>): List<MediaItem>

    // ---- Home / Discover --------------------------------------------------
    fun observeRecentlyAdded(limit: Int = 20): Flow<List<MediaItem>>
    fun observeRecentlyPlayed(limit: Int = 20): Flow<List<MediaItem>>
    fun observeMostPlayed(limit: Int = 20): Flow<List<MediaItem>>
    fun observeFavouriteSongs(): Flow<List<MediaItem>>
    fun observeFavouriteAlbums(): Flow<List<Album>>
    fun observeFavouriteArtists(): Flow<List<Artist>>
    fun observeSmartPlaylist(playlist: SmartPlaylist, limit: Int = 100): Flow<List<MediaItem>>

    // ---- Search -----------------------------------------------------------
    suspend fun search(query: String): SearchResults
    fun observeRecentSearches(limit: Int = 10): Flow<List<String>>
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()

    // ---- Mutations --------------------------------------------------------
    suspend fun setFavourite(mediaId: String, favourite: Boolean)
    suspend fun setAlbumFavourite(albumId: Long, favourite: Boolean)
    suspend fun setArtistFavourite(artistId: Long, favourite: Boolean)
    suspend fun setHidden(mediaId: String, hidden: Boolean)
    suspend fun setRating(mediaId: String, rating: Int)
    suspend fun updateMetadata(item: MediaItem)

    // ---- Playlists --------------------------------------------------------
    fun observePlaylists(): Flow<List<Playlist>>
    fun observePlaylistTracks(playlistId: Long): Flow<List<MediaItem>>
    suspend fun createPlaylist(name: String, description: String? = null): Long
    suspend fun renamePlaylist(playlistId: Long, name: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addToPlaylist(playlistId: Long, mediaIds: List<String>)
    suspend fun removeFromPlaylist(playlistId: Long, mediaIds: List<String>)
    suspend fun reorderPlaylist(playlistId: Long, orderedMediaIds: List<String>)
    suspend fun setPlaylistPinned(playlistId: Long, pinned: Boolean)

    // ---- History ----------------------------------------------------------
    fun observeHistory(limit: Int = 100): Flow<List<PlaybackHistoryEntry>>
    suspend fun recordPlayback(entry: PlaybackHistoryEntry)
    suspend fun clearHistory()

    // ---- Lyrics -----------------------------------------------------------
    suspend fun getLyrics(mediaId: String): Lyrics?
    suspend fun saveLyrics(lyrics: Lyrics)

    // ---- Scanning ---------------------------------------------------------
    val scanState: Flow<ScanState>
    suspend fun rescan(force: Boolean = false)
}

data class SearchResults(
    val songs: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val folders: List<Folder> = emptyList(),
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() &&
            playlists.isEmpty() && folders.isEmpty()

    val totalCount: Int
        get() = songs.size + albums.size + artists.size + playlists.size + folders.size
}

sealed interface ScanState {
    data object Idle : ScanState
    data class Scanning(val scanned: Int, val total: Int) : ScanState {
        val progress: Float get() = if (total > 0) scanned.toFloat() / total else 0f
    }
    data class Complete(val trackCount: Int, val finishedAtEpochMillis: Long) : ScanState
    data class Failed(val message: String) : ScanState
}
