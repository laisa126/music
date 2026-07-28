package com.aurora.music.data.repository

import com.aurora.music.core.common.AppDispatchers
import com.aurora.music.core.common.fuzzyScore
import com.aurora.music.data.database.dao.CollectionStateDao
import com.aurora.music.data.database.dao.HistoryDao
import com.aurora.music.data.database.dao.LyricsDao
import com.aurora.music.data.database.dao.PlaylistDao
import com.aurora.music.data.database.dao.RecentSearchDao
import com.aurora.music.data.database.dao.SongDao
import com.aurora.music.data.database.entity.AlbumStateEntity
import com.aurora.music.data.database.entity.ArtistStateEntity
import com.aurora.music.data.database.entity.PlaylistEntity
import com.aurora.music.data.database.entity.PlaylistSongEntity
import com.aurora.music.data.database.entity.RecentSearchEntity
import com.aurora.music.data.database.entity.SongEntity
import com.aurora.music.data.mediastore.MediaStoreScanner
import com.aurora.music.domain.model.Album
import com.aurora.music.domain.model.Artist
import com.aurora.music.domain.model.Folder
import com.aurora.music.domain.model.Genre
import com.aurora.music.domain.model.Lyrics
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.PlaybackHistoryEntry
import com.aurora.music.domain.model.Playlist
import com.aurora.music.domain.model.SmartPlaylist
import com.aurora.music.domain.repository.MusicRepository
import com.aurora.music.domain.repository.ScanState
import com.aurora.music.domain.repository.SearchResults
import com.aurora.music.domain.repository.SettingsRepository
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Phase 1 implementation of [MusicRepository] backed by MediaStore + Room.
 *
 * Phase 2 adds `CatalogMusicRepository` and a merged implementation; nothing
 * above this layer changes.
 */
@Singleton
class LocalMusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val collectionStateDao: CollectionStateDao,
    private val playlistDao: PlaylistDao,
    private val historyDao: HistoryDao,
    private val lyricsDao: LyricsDao,
    private val recentSearchDao: RecentSearchDao,
    private val scanner: MediaStoreScanner,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: AppDispatchers,
) : MusicRepository {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    override val scanState: Flow<ScanState> = _scanState.asStateFlow()

    private val scanMutex = Mutex()

    // ---- Library ----------------------------------------------------------

    override fun observeSongs(): Flow<List<MediaItem>> =
        songDao.observeAll().map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeAlbums(): Flow<List<Album>> =
        combine(
            songDao.observeAll(),
            collectionStateDao.observeAlbumStates(),
        ) { songs, states ->
            val favourites = states.filter { it.isFavourite }.map { it.albumId }.toSet()
            val artwork = states.mapNotNull { s -> s.customArtworkUri?.let { s.albumId to it } }
                .toMap()
            songs.groupBy { it.albumId }
                .mapNotNull { (albumId, tracks) ->
                    val first = tracks.firstOrNull() ?: return@mapNotNull null
                    Album(
                        id = albumId,
                        title = first.album,
                        artist = first.albumArtist?.takeIf { it.isNotBlank() } ?: first.artist,
                        artistId = first.artistId,
                        artworkUri = artwork[albumId]
                            ?: tracks.firstNotNullOfOrNull { it.artworkUri },
                        year = tracks.maxOf { it.year },
                        trackCount = tracks.size,
                        totalDurationMs = tracks.sumOf { it.durationMs },
                        totalSizeBytes = tracks.sumOf { it.fileSizeBytes },
                        genre = tracks.firstNotNullOfOrNull { it.genre },
                        isFavourite = albumId in favourites,
                    )
                }
                .sortedBy { it.title.lowercase() }
        }.flowOn(dispatchers.default)

    override fun observeArtists(): Flow<List<Artist>> =
        combine(
            songDao.observeAll(),
            collectionStateDao.observeArtistStates(),
        ) { songs, states ->
            val favourites = states.filter { it.isFavourite }.map { it.artistId }.toSet()
            val artwork = states.mapNotNull { s -> s.customArtworkUri?.let { s.artistId to it } }
                .toMap()
            songs.groupBy { it.artistId }
                .mapNotNull { (artistId, tracks) ->
                    val first = tracks.firstOrNull() ?: return@mapNotNull null
                    Artist(
                        id = artistId,
                        name = first.artist,
                        albumCount = tracks.map { it.albumId }.distinct().size,
                        trackCount = tracks.size,
                        artworkUri = artwork[artistId]
                            ?: tracks.firstNotNullOfOrNull { it.artworkUri },
                        totalPlayCount = tracks.sumOf { it.playCount },
                        isFavourite = artistId in favourites,
                    )
                }
                .sortedBy { it.name.lowercase() }
        }.flowOn(dispatchers.default)

    override fun observeGenres(): Flow<List<Genre>> =
        songDao.observeAll().map { songs ->
            songs.filter { !it.genre.isNullOrBlank() }
                .groupBy { it.genre!! }
                .map { (name, tracks) ->
                    Genre(
                        id = name.hashCode().toLong(),
                        name = name,
                        trackCount = tracks.size,
                        albumCount = tracks.map { it.albumId }.distinct().size,
                        artistCount = tracks.map { it.artistId }.distinct().size,
                        artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                    )
                }
                .sortedBy { it.name.lowercase() }
        }.flowOn(dispatchers.default)

    override fun observeFolders(): Flow<List<Folder>> =
        combine(
            songDao.observeAll(),
            collectionStateDao.observeFolderStates(),
        ) { songs, states ->
            val stateByPath = states.associateBy { it.path }
            songs.filter { !it.folderPath.isNullOrBlank() }
                .groupBy { it.folderPath!! }
                .map { (path, tracks) ->
                    val state = stateByPath[path]
                    Folder(
                        path = path,
                        name = state?.displayName ?: File(path).name.ifBlank { path },
                        trackCount = tracks.size,
                        totalSizeBytes = tracks.sumOf { it.fileSizeBytes },
                        artworkUri = tracks.firstNotNullOfOrNull { it.artworkUri },
                        isFavourite = state?.isFavourite == true,
                        isExcluded = state?.isExcluded == true,
                    )
                }
                .sortedBy { it.name.lowercase() }
        }.flowOn(dispatchers.default)

    override fun observeSong(id: String): Flow<MediaItem?> =
        songDao.observeById(id).map { it?.toDomain() }.flowOn(dispatchers.io)

    override fun observeAlbumTracks(albumId: Long): Flow<List<MediaItem>> =
        songDao.observeByAlbum(albumId).map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeArtistTracks(artistId: Long): Flow<List<MediaItem>> =
        songDao.observeByArtist(artistId).map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeGenreTracks(genreName: String): Flow<List<MediaItem>> =
        songDao.observeByGenre(genreName).map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeFolderTracks(folderPath: String): Flow<List<MediaItem>> =
        songDao.observeByFolder(folderPath).map { it.toDomain() }.flowOn(dispatchers.io)

    override suspend fun getSong(id: String): MediaItem? = withContext(dispatchers.io) {
        songDao.getById(id)?.toDomain()
    }

    override suspend fun getSongs(ids: List<String>): List<MediaItem> =
        withContext(dispatchers.io) {
            if (ids.isEmpty()) return@withContext emptyList()
            val byId = songDao.getByIds(ids).associateBy { it.id }
            ids.mapNotNull { byId[it]?.toDomain() }
        }

    // ---- Home / Discover --------------------------------------------------

    override fun observeRecentlyAdded(limit: Int): Flow<List<MediaItem>> =
        songDao.observeRecentlyAdded(limit).map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeRecentlyPlayed(limit: Int): Flow<List<MediaItem>> =
        songDao.observeRecentlyPlayed(limit).map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeMostPlayed(limit: Int): Flow<List<MediaItem>> =
        songDao.observeMostPlayed(limit).map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeFavouriteSongs(): Flow<List<MediaItem>> =
        songDao.observeFavourites().map { it.toDomain() }.flowOn(dispatchers.io)

    override fun observeFavouriteAlbums(): Flow<List<Album>> =
        observeAlbums().map { albums -> albums.filter { it.isFavourite } }

    override fun observeFavouriteArtists(): Flow<List<Artist>> =
        observeArtists().map { artists -> artists.filter { it.isFavourite } }

    override fun observeSmartPlaylist(
        playlist: SmartPlaylist,
        limit: Int,
    ): Flow<List<MediaItem>> {
        val now = System.currentTimeMillis()
        val entities: Flow<List<SongEntity>> = when (playlist) {
            SmartPlaylist.MOST_PLAYED -> songDao.observeMostPlayed(limit)
            SmartPlaylist.RECENTLY_ADDED -> songDao.observeRecentlyAdded(limit)
            SmartPlaylist.RECENTLY_PLAYED -> songDao.observeRecentlyPlayed(limit)
            SmartPlaylist.NEVER_PLAYED -> songDao.observeNeverPlayed(limit)
            SmartPlaylist.FAVOURITES -> songDao.observeFavourites()
            SmartPlaylist.HIGH_QUALITY -> songDao.observeHighQuality(limit)
            SmartPlaylist.FLAC_COLLECTION -> songDao.observeFlac(limit)
            SmartPlaylist.LONGEST -> songDao.observeLongest(limit)
            SmartPlaylist.SHORTEST -> songDao.observeShortest(limit)
            SmartPlaylist.HIDDEN_GEMS -> songDao.observeHiddenGems(limit)
            SmartPlaylist.ADDED_THIS_WEEK -> songDao.observeAddedSince(secondsAgo(now, 7), limit)
            SmartPlaylist.ADDED_THIS_MONTH -> songDao.observeAddedSince(secondsAgo(now, 30), limit)
            SmartPlaylist.ADDED_THIS_YEAR -> songDao.observeAddedSince(secondsAgo(now, 365), limit)
        }
        return entities.map { it.toDomain() }.flowOn(dispatchers.io)
    }

    private fun secondsAgo(now: Long, days: Long): Long =
        (now - TimeUnit.DAYS.toMillis(days)) / 1000

    // ---- Search -----------------------------------------------------------

    override suspend fun search(query: String): SearchResults = withContext(dispatchers.default) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext SearchResults()

        val songs = songDao.search(trimmed, limit = 100).toDomain()
        // Fuzzy fallback: if the literal LIKE query found little, rank the whole
        // library by similarity so typos still surface results.
        val ranked = if (songs.size < 5) {
            val all = songDao.observeAll().first()
            (songs + all.toDomain())
                .distinctBy { it.id }
                .map { it to maxOf(fuzzyScore(trimmed, it.title), fuzzyScore(trimmed, it.artist)) }
                .filter { it.second > 0.45f }
                .sortedByDescending { it.second }
                .take(50)
                .map { it.first }
        } else {
            songs
        }

        val albums = observeAlbums().first()
            .filter { it.title.contains(trimmed, true) || it.artist.contains(trimmed, true) }
        val artists = observeArtists().first().filter { it.name.contains(trimmed, true) }
        val folders = observeFolders().first().filter { it.name.contains(trimmed, true) }
        val playlists = playlistDao.search(trimmed).map { it.toDomain() }

        SearchResults(
            songs = ranked,
            albums = albums.take(30),
            artists = artists.take(30),
            playlists = playlists,
            folders = folders.take(30),
        )
    }

    override fun observeRecentSearches(limit: Int): Flow<List<String>> =
        recentSearchDao.observeRecent(limit).map { rows -> rows.map { it.query } }
            .flowOn(dispatchers.io)

    override suspend fun addRecentSearch(query: String) = withContext(dispatchers.io) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext
        recentSearchDao.upsert(
            RecentSearchEntity(query = trimmed, searchedAt = System.currentTimeMillis()),
        )
    }

    override suspend fun clearRecentSearches() = withContext(dispatchers.io) {
        recentSearchDao.clear()
    }

    // ---- Mutations --------------------------------------------------------

    override suspend fun setFavourite(mediaId: String, favourite: Boolean) =
        withContext(dispatchers.io) { songDao.setFavourite(mediaId, favourite) }

    override suspend fun setAlbumFavourite(albumId: Long, favourite: Boolean) =
        withContext(dispatchers.io) {
            val existing = collectionStateDao.getAlbumState(albumId)
            collectionStateDao.upsertAlbumState(
                existing?.copy(isFavourite = favourite)
                    ?: AlbumStateEntity(albumId = albumId, isFavourite = favourite),
            )
        }

    override suspend fun setArtistFavourite(artistId: Long, favourite: Boolean) =
        withContext(dispatchers.io) {
            val existing = collectionStateDao.getArtistState(artistId)
            collectionStateDao.upsertArtistState(
                existing?.copy(isFavourite = favourite)
                    ?: ArtistStateEntity(artistId = artistId, isFavourite = favourite),
            )
        }

    override suspend fun setHidden(mediaId: String, hidden: Boolean) =
        withContext(dispatchers.io) { songDao.setHidden(mediaId, hidden) }

    override suspend fun setRating(mediaId: String, rating: Int) =
        withContext(dispatchers.io) { songDao.setRating(mediaId, rating.coerceIn(0, 5)) }

    override suspend fun updateMetadata(item: MediaItem) = withContext(dispatchers.io) {
        songDao.updateMetadata(
            id = item.id,
            title = item.title,
            artist = item.artist,
            album = item.album,
            albumArtist = item.albumArtist,
            composer = item.composer,
            genre = item.genre,
            year = item.year,
            trackNumber = item.trackNumber,
            discNumber = item.discNumber,
            artworkUri = item.artworkUri,
        )
    }

    // ---- Playlists --------------------------------------------------------

    override fun observePlaylists(): Flow<List<Playlist>> =
        combine(
            playlistDao.observeAll(),
            playlistDao.observeAllStats(),
        ) { playlists, stats ->
            val byId = stats.associateBy { it.playlistId }
            playlists.map { entity ->
                val stat = byId[entity.id]
                entity.toDomain(
                    trackCount = stat?.trackCount ?: 0,
                    totalDurationMs = stat?.totalDuration ?: 0L,
                )
            }
        }.flowOn(dispatchers.io)

    override fun observePlaylistTracks(playlistId: Long): Flow<List<MediaItem>> =
        playlistDao.observeTracks(playlistId).map { it.toDomain() }.flowOn(dispatchers.io)

    override suspend fun createPlaylist(name: String, description: String?): Long =
        withContext(dispatchers.io) {
            val now = System.currentTimeMillis()
            playlistDao.insert(
                PlaylistEntity(
                    name = name.trim().ifBlank { "New playlist" },
                    description = description,
                    createdAt = now,
                    lastModified = now,
                ),
            )
        }

    override suspend fun renamePlaylist(playlistId: Long, name: String) =
        withContext(dispatchers.io) {
            playlistDao.rename(playlistId, name.trim(), System.currentTimeMillis())
        }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(dispatchers.io) {
        playlistDao.deleteWithTracks(playlistId)
    }

    override suspend fun addToPlaylist(playlistId: Long, mediaIds: List<String>) =
        withContext(dispatchers.io) {
            if (mediaIds.isEmpty()) return@withContext
            val now = System.currentTimeMillis()
            val start = playlistDao.maxPosition(playlistId) + 1
            playlistDao.insertTracks(
                mediaIds.mapIndexed { index, id ->
                    PlaylistSongEntity(playlistId, id, start + index, now)
                },
            )
            playlistDao.touch(playlistId, now)
        }

    override suspend fun removeFromPlaylist(playlistId: Long, mediaIds: List<String>) =
        withContext(dispatchers.io) { playlistDao.removeTracks(playlistId, mediaIds) }

    override suspend fun reorderPlaylist(playlistId: Long, orderedMediaIds: List<String>) =
        withContext(dispatchers.io) {
            playlistDao.reorder(playlistId, orderedMediaIds, System.currentTimeMillis())
        }

    override suspend fun setPlaylistPinned(playlistId: Long, pinned: Boolean) =
        withContext(dispatchers.io) {
            playlistDao.setPinned(playlistId, pinned, System.currentTimeMillis())
        }

    // ---- History ----------------------------------------------------------

    override fun observeHistory(limit: Int): Flow<List<PlaybackHistoryEntry>> =
        historyDao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun recordPlayback(entry: PlaybackHistoryEntry) =
        withContext(dispatchers.io) {
            if (!settingsRepository.current().historyRecordingEnabled) return@withContext
            historyDao.insert(entry.toEntity())
            if (entry.wasSkipped) {
                songDao.incrementSkipCount(entry.mediaId)
            } else {
                songDao.incrementPlayCount(entry.mediaId, entry.playedAtEpochMillis)
            }
        }

    override suspend fun clearHistory() = withContext(dispatchers.io) { historyDao.clear() }

    // ---- Lyrics -----------------------------------------------------------

    override suspend fun getLyrics(mediaId: String): Lyrics? = withContext(dispatchers.io) {
        lyricsDao.get(mediaId)?.toDomain() ?: loadSidecarLrc(mediaId)
    }

    override suspend fun saveLyrics(lyrics: Lyrics) = withContext(dispatchers.io) {
        lyricsDao.upsert(lyrics.toEntity())
    }

    private suspend fun loadSidecarLrc(mediaId: String): Lyrics? {
        val path = songDao.getById(mediaId)?.filePath ?: return null
        val lrc = File(path.substringBeforeLast('.') + ".lrc")
        return runCatching {
            if (lrc.exists() && lrc.canRead()) {
                parseLrc(lrc.readText()).copy(mediaId = mediaId)
            } else {
                null
            }
        }.getOrNull()
    }

    // ---- Scanning ---------------------------------------------------------

    override suspend fun rescan(force: Boolean) {
        if (scanMutex.isLocked && !force) return
        scanMutex.withLock {
            withContext(dispatchers.io) {
                if (!scanner.hasAudioPermission()) {
                    _scanState.value = ScanState.Failed("Audio permission not granted")
                    return@withContext
                }
                _scanState.value = ScanState.Scanning(0, 0)
                runCatching {
                    val settings = settingsRepository.current()
                    val stamp = System.currentTimeMillis()
                    val scanned = scanner.scan(
                        minDurationSeconds = settings.minTrackDurationSeconds,
                        excludedFolders = settings.excludedFolders,
                        includedFolders = settings.includedFolders,
                        onProgress = { done, total ->
                            _scanState.value = ScanState.Scanning(done, total)
                        },
                    )
                    // Preserve user-owned state (favourites, counts) across rescans.
                    val existing = songDao.getByIds(scanned.map { it.id }).associateBy { it.id }
                    val merged = scanned.map { fresh ->
                        val old = existing[fresh.id] ?: return@map fresh
                        fresh.copy(
                            playCount = old.playCount,
                            lastPlayed = old.lastPlayed,
                            isFavourite = old.isFavourite,
                            isHidden = old.isHidden,
                            rating = old.rating,
                            skipCount = old.skipCount,
                        )
                    }
                    merged.chunked(400).forEach { songDao.upsertAll(it) }
                    songDao.deleteStale(stamp)
                    ScanState.Complete(merged.size, System.currentTimeMillis())
                }.onSuccess {
                    _scanState.value = it
                }.onFailure { error ->
                    _scanState.value = ScanState.Failed(
                        error.message ?: "Scan failed. Please try again.",
                    )
                }
            }
        }
    }
}
