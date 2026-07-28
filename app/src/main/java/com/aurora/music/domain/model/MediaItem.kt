package com.aurora.music.domain.model

/**
 * Where a playable item originates from.
 *
 * Phase 1 only ever produces [LOCAL]. Phase 2 adds a remote catalog without
 * changing any consumer of [MediaItem] — see Section 2a / Section 13 of the spec.
 */
enum class MediaSource {
    LOCAL,
    REMOTE_CATALOG,
}

/**
 * The single domain model for anything playable, regardless of origin.
 *
 * Deliberately *not* named `LocalSong`: UI, queue, player and notification code
 * all speak this type so a remote catalog can be layered in additively.
 */
data class MediaItem(
    val id: String,
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
    val source: MediaSource = MediaSource.LOCAL,
    /** Content/file URI for local items. Null for remote-only catalog items. */
    val localUri: String? = null,
    /** Catalog identifier for remote items. Null for local-only items. */
    val remoteId: String? = null,
    /** Stream URL for remote items. Null for local items. */
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
    val dateAddedEpochSeconds: Long = 0L,
    val dateModifiedEpochSeconds: Long = 0L,
    val playCount: Int = 0,
    val lastPlayedEpochMillis: Long = 0L,
    val isFavourite: Boolean = false,
    val isHidden: Boolean = false,
    val rating: Int = 0,
    val folderPath: String? = null,
    val isDownloaded: Boolean = source == MediaSource.LOCAL,
) {
    /**
     * The URI the player should actually load. Resolving here (rather than in
     * `PlayerManager` or the UI) is what keeps playback source-agnostic.
     */
    val playbackUri: String?
        get() = when (source) {
            MediaSource.LOCAL -> localUri
            MediaSource.REMOTE_CATALOG -> streamUrl ?: localUri
        }

    val isPlayable: Boolean get() = !playbackUri.isNullOrBlank()

    /** Human-readable quality badge, e.g. "FLAC", "MP3". */
    val qualityBadge: String
        get() = AudioFormat.fromMimeTypeOrExtension(mimeType, fileName).label

    val isLossless: Boolean
        get() = AudioFormat.fromMimeTypeOrExtension(mimeType, fileName).lossless
}

enum class AudioFormat(val label: String, val lossless: Boolean) {
    MP3("MP3", false),
    AAC("AAC", false),
    M4A("M4A", false),
    FLAC("FLAC", true),
    ALAC("ALAC", true),
    WAV("WAV", true),
    AIFF("AIFF", true),
    OGG("OGG", false),
    OPUS("OPUS", false),
    APE("APE", true),
    DSF("DSF", true),
    DFF("DFF", true),
    UNKNOWN("AUDIO", false),
    ;

    companion object {
        fun fromMimeTypeOrExtension(mimeType: String?, fileName: String?): AudioFormat {
            val ext = fileName?.substringAfterLast('.', "")?.lowercase().orEmpty()
            byExtension(ext)?.let { return it }
            val mime = mimeType?.lowercase().orEmpty()
            return when {
                mime.contains("flac") -> FLAC
                mime.contains("mpeg") || mime.contains("mp3") -> MP3
                mime.contains("aac") -> AAC
                mime.contains("mp4") || mime.contains("m4a") -> M4A
                mime.contains("wav") -> WAV
                mime.contains("aiff") -> AIFF
                mime.contains("opus") -> OPUS
                mime.contains("ogg") || mime.contains("vorbis") -> OGG
                else -> UNKNOWN
            }
        }

        private fun byExtension(ext: String): AudioFormat? = when (ext) {
            "mp3" -> MP3
            "aac" -> AAC
            "m4a", "m4b" -> M4A
            "flac" -> FLAC
            "alac" -> ALAC
            "wav", "wave" -> WAV
            "aif", "aiff" -> AIFF
            "ogg", "oga" -> OGG
            "opus" -> OPUS
            "ape" -> APE
            "dsf" -> DSF
            "dff" -> DFF
            else -> null
        }
    }
}
