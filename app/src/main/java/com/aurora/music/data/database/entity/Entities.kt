package com.aurora.music.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index("title"),
        Index("artist"),
        Index("album"),
        Index("albumId"),
        Index("artistId"),
        Index("genre"),
        Index("playCount"),
        Index("dateAdded"),
        Index("folderPath"),
    ],
)
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String? = null,
    val composer: String? = null,
    val genre: String? = null,
    val durationMs: Long = 0L,
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val year: Int = 0,
    val source: String = "LOCAL",
    val localUri: String? = null,
    val remoteId: String? = null,
    val streamUrl: String? = null,
    val artworkUri: String? = null,
    val albumId: Long = 0L,
    val artistId: Long = 0L,
    val filePath: String? = null,
    val fileName: String? = null,
    val fileSizeBytes: Long = 0L,
    val mimeType: String? = null,
    val bitrateKbps: Int = 0,
    val sampleRateHz: Int = 0,
    val bitDepth: Int = 0,
    val channels: Int = 0,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val folderPath: String? = null,
    // Local, user-owned state
    val playCount: Int = 0,
    val lastPlayed: Long = 0L,
    val isFavourite: Boolean = false,
    val isHidden: Boolean = false,
    val rating: Int = 0,
    val skipCount: Int = 0,
    /** Set on each scan pass so stale rows can be pruned. */
    val lastSeen: Long = 0L,
)

@Entity(tableName = "album_state")
data class AlbumStateEntity(
    @PrimaryKey val albumId: Long,
    val isFavourite: Boolean = false,
    val isHidden: Boolean = false,
    val customArtworkUri: String? = null,
)

@Entity(tableName = "artist_state")
data class ArtistStateEntity(
    @PrimaryKey val artistId: Long,
    val isFavourite: Boolean = false,
    val isHidden: Boolean = false,
    val customArtworkUri: String? = null,
    val notes: String? = null,
)

@Entity(tableName = "folder_state")
data class FolderStateEntity(
    @PrimaryKey val path: String,
    val isFavourite: Boolean = false,
    val isExcluded: Boolean = false,
    val displayName: String? = null,
)

@Entity(tableName = "playlists", indices = [Index("name")])
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val type: String = "MANUAL",
    val createdAt: Long = 0L,
    val lastModified: Long = 0L,
    val isFavourite: Boolean = false,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val smartRule: String? = null,
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "mediaId"],
    indices = [Index("playlistId"), Index("mediaId"), Index("position")],
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val mediaId: String,
    val position: Int,
    val addedAt: Long = 0L,
)

@Entity(
    tableName = "history",
    indices = [Index("mediaId"), Index("playedAt")],
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mediaId: String,
    val playedAt: Long,
    val listenedDurationMs: Long,
    val completionPercent: Float,
    val wasSkipped: Boolean,
)

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val mediaId: String,
    val plainText: String? = null,
    /** Serialized as `timeMs|text` lines. */
    val syncedText: String? = null,
    val offsetMs: Long = 0L,
)

@Entity(tableName = "equalizer_presets", indices = [Index(value = ["name"], unique = true)])
data class EqualizerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** Comma-separated dB gains, one per band. */
    val gains: String,
    val isBuiltIn: Boolean = false,
)

@Entity(tableName = "recent_searches", indices = [Index(value = ["query"], unique = true)])
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val query: String,
    val searchedAt: Long,
)

@Entity(tableName = "queue", indices = [Index("position")])
data class QueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mediaId: String,
    val position: Int,
)
