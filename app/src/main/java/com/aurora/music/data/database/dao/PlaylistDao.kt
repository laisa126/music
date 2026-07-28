package com.aurora.music.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.aurora.music.data.database.entity.EqualizerPresetEntity
import com.aurora.music.data.database.entity.HistoryEntity
import com.aurora.music.data.database.entity.LyricsEntity
import com.aurora.music.data.database.entity.PlaylistEntity
import com.aurora.music.data.database.entity.PlaylistSongEntity
import com.aurora.music.data.database.entity.QueueEntity
import com.aurora.music.data.database.entity.RecentSearchEntity
import com.aurora.music.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, lastModified DESC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observeById(id: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name, lastModified = :now WHERE id = :id")
    suspend fun rename(id: Long, name: String, now: Long)

    @Query("UPDATE playlists SET isPinned = :pinned, lastModified = :now WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, now: Long)

    @Query("UPDATE playlists SET lastModified = :now WHERE id = :id")
    suspend fun touch(id: Long, now: Long)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id")
    suspend fun deleteTracksOf(id: Long)

    @Transaction
    suspend fun deleteWithTracks(id: Long) {
        deleteTracksOf(id)
        delete(id)
    }

    @Query(
        """
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON ps.mediaId = s.id
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC
        """,
    )
    fun observeTracks(playlistId: Long): Flow<List<SongEntity>>

    @Query(
        """
        SELECT COUNT(*) AS trackCount, COALESCE(SUM(s.durationMs), 0) AS totalDuration
        FROM songs s INNER JOIN playlist_songs ps ON ps.mediaId = s.id
        WHERE ps.playlistId = :playlistId
        """,
    )
    fun observeStats(playlistId: Long): Flow<PlaylistStats?>

    @Query(
        """
        SELECT ps.playlistId AS playlistId, COUNT(*) AS trackCount,
               COALESCE(SUM(s.durationMs), 0) AS totalDuration
        FROM playlist_songs ps INNER JOIN songs s ON ps.mediaId = s.id
        GROUP BY ps.playlistId
        """,
    )
    fun observeAllStats(): Flow<List<PlaylistStatsRow>>

    @Query(
        """
        SELECT s.artworkUri FROM songs s
        INNER JOIN playlist_songs ps ON ps.mediaId = s.id
        WHERE ps.playlistId = :playlistId AND s.artworkUri IS NOT NULL
        ORDER BY ps.position ASC LIMIT 4
        """,
    )
    suspend fun artworkForCollage(playlistId: Long): List<String>

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(entries: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND mediaId IN (:mediaIds)")
    suspend fun removeTracks(playlistId: Long, mediaIds: List<String>)

    @Transaction
    suspend fun reorder(playlistId: Long, orderedMediaIds: List<String>, now: Long) {
        deleteTracksOf(playlistId)
        insertTracks(
            orderedMediaIds.mapIndexed { index, mediaId ->
                PlaylistSongEntity(playlistId, mediaId, index, now)
            },
        )
    }
}

data class PlaylistStats(val trackCount: Int, val totalDuration: Long)

data class PlaylistStatsRow(
    val playlistId: Long,
    val trackCount: Int,
    val totalDuration: Long,
)

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entry: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clear()

    @Query("DELETE FROM history WHERE playedAt < :threshold")
    suspend fun trim(threshold: Long)
}

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics WHERE mediaId = :mediaId")
    suspend fun get(mediaId: String): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lyrics: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE mediaId = :mediaId")
    suspend fun delete(mediaId: String)
}

@Dao
interface EqualizerPresetDao {

    @Query("SELECT * FROM equalizer_presets ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preset: EqualizerPresetEntity): Long

    @Query("DELETE FROM equalizer_presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun delete(id: Long)
}

@Dao
interface RecentSearchDao {

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clear()

    @Query("DELETE FROM recent_searches WHERE `query` = :query")
    suspend fun delete(query: String)
}

@Dao
interface QueueDao {

    @Query("SELECT * FROM queue ORDER BY position ASC")
    suspend fun get(): List<QueueEntity>

    @Query("DELETE FROM queue")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<QueueEntity>)

    @Transaction
    suspend fun replace(mediaIds: List<String>) {
        clear()
        insertAll(mediaIds.mapIndexed { index, id -> QueueEntity(mediaId = id, position = index) })
    }
}
