package com.aurora.music.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.aurora.music.data.database.entity.AlbumStateEntity
import com.aurora.music.data.database.entity.ArtistStateEntity
import com.aurora.music.data.database.entity.FolderStateEntity
import com.aurora.music.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs WHERE isHidden = 0 ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun observeAllIncludingHidden(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    fun observeById(id: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<SongEntity>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM songs")
    fun observeCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND albumId = :albumId
        ORDER BY discNumber ASC, trackNumber ASC, title COLLATE NOCASE ASC
        """,
    )
    fun observeByAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND artistId = :artistId
        ORDER BY album COLLATE NOCASE ASC, discNumber ASC, trackNumber ASC
        """,
    )
    fun observeByArtist(artistId: Long): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND genre = :genre
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeByGenre(genre: String): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND folderPath = :folderPath
        ORDER BY title COLLATE NOCASE ASC
        """,
    )
    fun observeByFolder(folderPath: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isHidden = 0 ORDER BY dateAdded DESC LIMIT :limit")
    fun observeRecentlyAdded(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND lastPlayed > 0
        ORDER BY lastPlayed DESC LIMIT :limit
        """,
    )
    fun observeRecentlyPlayed(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND playCount > 0
        ORDER BY playCount DESC, lastPlayed DESC LIMIT :limit
        """,
    )
    fun observeMostPlayed(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isHidden = 0 AND playCount = 0 ORDER BY title LIMIT :limit")
    fun observeNeverPlayed(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavourite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun observeFavourites(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isHidden = 0 ORDER BY durationMs DESC LIMIT :limit")
    fun observeLongest(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND durationMs > 0
        ORDER BY durationMs ASC LIMIT :limit
        """,
    )
    fun observeShortest(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND dateAdded >= :since
        ORDER BY dateAdded DESC LIMIT :limit
        """,
    )
    fun observeAddedSince(since: Long, limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND (rating >= 4 OR isFavourite = 1)
        AND playCount <= 2 ORDER BY rating DESC, title ASC LIMIT :limit
        """,
    )
    fun observeHiddenGems(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND (
            LOWER(mimeType) LIKE '%flac%' OR LOWER(fileName) LIKE '%.flac'
        ) ORDER BY title COLLATE NOCASE ASC LIMIT :limit
        """,
    )
    fun observeFlac(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND (bitrateKbps >= 320 OR sampleRateHz > 44100)
        ORDER BY bitrateKbps DESC LIMIT :limit
        """,
    )
    fun observeHighQuality(limit: Int): Flow<List<SongEntity>>

    @Query(
        """
        SELECT * FROM songs WHERE isHidden = 0 AND (
            title LIKE '%' || :query || '%' OR
            artist LIKE '%' || :query || '%' OR
            album LIKE '%' || :query || '%' OR
            genre LIKE '%' || :query || '%' OR
            composer LIKE '%' || :query || '%' OR
            fileName LIKE '%' || :query || '%'
        ) ORDER BY
            CASE WHEN title LIKE :query || '%' THEN 0 ELSE 1 END,
            title COLLATE NOCASE ASC
        LIMIT :limit
        """,
    )
    suspend fun search(query: String, limit: Int): List<SongEntity>

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE lastSeen < :threshold AND source = 'LOCAL'")
    suspend fun deleteStale(threshold: Long): Int

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE songs SET isFavourite = :favourite WHERE id = :id")
    suspend fun setFavourite(id: String, favourite: Boolean)

    @Query("UPDATE songs SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("UPDATE songs SET rating = :rating WHERE id = :id")
    suspend fun setRating(id: String, rating: Int)

    @Query(
        """
        UPDATE songs SET playCount = playCount + 1, lastPlayed = :playedAt WHERE id = :id
        """,
    )
    suspend fun incrementPlayCount(id: String, playedAt: Long)

    @Query("UPDATE songs SET skipCount = skipCount + 1 WHERE id = :id")
    suspend fun incrementSkipCount(id: String)

    @Query(
        """
        UPDATE songs SET title = :title, artist = :artist, album = :album,
            albumArtist = :albumArtist, composer = :composer, genre = :genre,
            year = :year, trackNumber = :trackNumber, discNumber = :discNumber,
            artworkUri = :artworkUri
        WHERE id = :id
        """,
    )
    suspend fun updateMetadata(
        id: String,
        title: String,
        artist: String,
        album: String,
        albumArtist: String?,
        composer: String?,
        genre: String?,
        year: Int,
        trackNumber: Int,
        discNumber: Int,
        artworkUri: String?,
    )
}

@Dao
interface CollectionStateDao {

    @Query("SELECT * FROM album_state")
    fun observeAlbumStates(): Flow<List<AlbumStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbumState(state: AlbumStateEntity)

    @Query("SELECT * FROM album_state WHERE albumId = :albumId")
    suspend fun getAlbumState(albumId: Long): AlbumStateEntity?

    @Query("SELECT * FROM artist_state")
    fun observeArtistStates(): Flow<List<ArtistStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArtistState(state: ArtistStateEntity)

    @Query("SELECT * FROM artist_state WHERE artistId = :artistId")
    suspend fun getArtistState(artistId: Long): ArtistStateEntity?

    @Query("SELECT * FROM folder_state")
    fun observeFolderStates(): Flow<List<FolderStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolderState(state: FolderStateEntity)
}
