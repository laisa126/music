package com.aurora.music.domain.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long = 0L,
    val artworkUri: String? = null,
    val year: Int = 0,
    val trackCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val totalSizeBytes: Long = 0L,
    val genre: String? = null,
    val isFavourite: Boolean = false,
    val source: MediaSource = MediaSource.LOCAL,
)

data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
    val artworkUri: String? = null,
    val totalPlayCount: Int = 0,
    val isFavourite: Boolean = false,
    val source: MediaSource = MediaSource.LOCAL,
)

data class Genre(
    val id: Long,
    val name: String,
    val trackCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val artworkUri: String? = null,
)

data class Folder(
    val path: String,
    val name: String,
    val trackCount: Int = 0,
    val totalSizeBytes: Long = 0L,
    val artworkUri: String? = null,
    val isFavourite: Boolean = false,
    val isExcluded: Boolean = false,
)

enum class PlaylistType { MANUAL, SMART, TEMPORARY, IMPORTED }

data class Playlist(
    val id: Long,
    val name: String,
    val description: String? = null,
    val type: PlaylistType = PlaylistType.MANUAL,
    val trackCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val artworkUris: List<String> = emptyList(),
    val lastModifiedEpochMillis: Long = 0L,
    val isFavourite: Boolean = false,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val smartRule: String? = null,
)

/** A rule-based playlist definition (Section 9). */
enum class SmartPlaylist(val title: String) {
    MOST_PLAYED("Most Played"),
    RECENTLY_ADDED("Recently Added"),
    RECENTLY_PLAYED("Recently Played"),
    NEVER_PLAYED("Never Played"),
    FAVOURITES("Favourite Songs"),
    HIGH_QUALITY("High Quality Audio"),
    FLAC_COLLECTION("FLAC Collection"),
    LONGEST("Longest Songs"),
    SHORTEST("Shortest Songs"),
    ADDED_THIS_WEEK("Added This Week"),
    ADDED_THIS_MONTH("Added This Month"),
    ADDED_THIS_YEAR("Added This Year"),
    HIDDEN_GEMS("Hidden Gems"),
}

data class PlaybackHistoryEntry(
    val id: Long = 0L,
    val mediaId: String,
    val playedAtEpochMillis: Long,
    val listenedDurationMs: Long,
    val completionPercent: Float,
    val wasSkipped: Boolean,
)

data class Lyrics(
    val mediaId: String,
    val plainText: String? = null,
    val synced: List<LyricLine> = emptyList(),
    val offsetMs: Long = 0L,
) {
    val isSynced: Boolean get() = synced.isNotEmpty()
    val isEmpty: Boolean get() = plainText.isNullOrBlank() && synced.isEmpty()
}

data class LyricLine(val timeMs: Long, val text: String)

/** Mood collections surfaced on Home (Section 4). */
enum class Mood(val title: String, val keywords: List<String>) {
    RELAX("Relax", listOf("chill", "ambient", "acoustic", "lofi", "calm")),
    WORKOUT("Workout", listOf("edm", "rock", "hip hop", "dance", "metal")),
    FOCUS("Focus", listOf("instrumental", "classical", "piano", "ambient")),
    SLEEP("Sleep", listOf("ambient", "piano", "sleep", "calm")),
    TRAVEL("Travel", listOf("pop", "indie", "folk", "country")),
    PARTY("Party", listOf("dance", "edm", "pop", "house", "hip hop")),
    ROMANCE("Romance", listOf("r&b", "soul", "love", "ballad", "jazz")),
}
