package com.aurora.music.data.repository

import com.aurora.music.data.database.entity.EqualizerPresetEntity
import com.aurora.music.data.database.entity.HistoryEntity
import com.aurora.music.data.database.entity.LyricsEntity
import com.aurora.music.data.database.entity.PlaylistEntity
import com.aurora.music.data.database.entity.SongEntity
import com.aurora.music.domain.model.EQ_BANDS_HZ
import com.aurora.music.domain.model.EqualizerPreset
import com.aurora.music.domain.model.LyricLine
import com.aurora.music.domain.model.Lyrics
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.MediaSource
import com.aurora.music.domain.model.PlaybackHistoryEntry
import com.aurora.music.domain.model.Playlist
import com.aurora.music.domain.model.PlaylistType

fun SongEntity.toDomain(): MediaItem = MediaItem(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumArtist = albumArtist,
    composer = composer,
    genre = genre,
    durationMs = durationMs,
    trackNumber = trackNumber,
    discNumber = discNumber,
    year = year,
    source = runCatching { MediaSource.valueOf(source) }.getOrDefault(MediaSource.LOCAL),
    localUri = localUri,
    remoteId = remoteId,
    streamUrl = streamUrl,
    artworkUri = artworkUri,
    albumId = albumId,
    artistId = artistId,
    filePath = filePath,
    fileName = fileName,
    fileSizeBytes = fileSizeBytes,
    mimeType = mimeType,
    bitrateKbps = bitrateKbps,
    sampleRateHz = sampleRateHz,
    bitDepth = bitDepth,
    channels = channels,
    dateAddedEpochSeconds = dateAdded,
    dateModifiedEpochSeconds = dateModified,
    playCount = playCount,
    lastPlayedEpochMillis = lastPlayed,
    isFavourite = isFavourite,
    isHidden = isHidden,
    rating = rating,
    folderPath = folderPath,
)

fun List<SongEntity>.toDomain(): List<MediaItem> = map { it.toDomain() }

fun PlaylistEntity.toDomain(
    trackCount: Int = 0,
    totalDurationMs: Long = 0L,
    artwork: List<String> = emptyList(),
): Playlist = Playlist(
    id = id,
    name = name,
    description = description,
    type = runCatching { PlaylistType.valueOf(type) }.getOrDefault(PlaylistType.MANUAL),
    trackCount = trackCount,
    totalDurationMs = totalDurationMs,
    artworkUris = artwork,
    lastModifiedEpochMillis = lastModified,
    isFavourite = isFavourite,
    isPinned = isPinned,
    isHidden = isHidden,
    smartRule = smartRule,
)

fun HistoryEntity.toDomain(): PlaybackHistoryEntry = PlaybackHistoryEntry(
    id = id,
    mediaId = mediaId,
    playedAtEpochMillis = playedAt,
    listenedDurationMs = listenedDurationMs,
    completionPercent = completionPercent,
    wasSkipped = wasSkipped,
)

fun PlaybackHistoryEntry.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    mediaId = mediaId,
    playedAt = playedAtEpochMillis,
    listenedDurationMs = listenedDurationMs,
    completionPercent = completionPercent,
    wasSkipped = wasSkipped,
)

fun LyricsEntity.toDomain(): Lyrics = Lyrics(
    mediaId = mediaId,
    plainText = plainText,
    synced = syncedText?.let(::parseSyncedLyrics).orEmpty(),
    offsetMs = offsetMs,
)

fun Lyrics.toEntity(): LyricsEntity = LyricsEntity(
    mediaId = mediaId,
    plainText = plainText,
    syncedText = synced.takeIf { it.isNotEmpty() }
        ?.joinToString("\n") { "${it.timeMs}|${it.text}" },
    offsetMs = offsetMs,
)

fun EqualizerPresetEntity.toDomain(): EqualizerPreset {
    val parsed = gains.split(',').mapNotNull { it.trim().toFloatOrNull() }
    val normalized = when {
        parsed.size == EQ_BANDS_HZ.size -> parsed
        parsed.size > EQ_BANDS_HZ.size -> parsed.take(EQ_BANDS_HZ.size)
        else -> parsed + List(EQ_BANDS_HZ.size - parsed.size) { 0f }
    }
    return EqualizerPreset(id = id, name = name, gains = normalized, isBuiltIn = isBuiltIn)
}

fun EqualizerPreset.toEntity(): EqualizerPresetEntity = EqualizerPresetEntity(
    id = id,
    name = name,
    gains = gains.joinToString(","),
    isBuiltIn = isBuiltIn,
)

private fun parseSyncedLyrics(raw: String): List<LyricLine> =
    raw.lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf('|')
            if (separator <= 0) return@mapNotNull null
            val time = line.substring(0, separator).toLongOrNull() ?: return@mapNotNull null
            LyricLine(time, line.substring(separator + 1))
        }
        .sortedBy { it.timeMs }
        .toList()

/** Parses a standard `.lrc` file into synced lines. */
fun parseLrc(content: String): Lyrics {
    val tagRegex = Regex("""\[(\d{1,2}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    val lines = mutableListOf<LyricLine>()
    val plain = StringBuilder()

    content.lineSequence().forEach { rawLine ->
        val matches = tagRegex.findAll(rawLine).toList()
        val text = rawLine.replace(tagRegex, "").trim()
        if (text.isEmpty()) return@forEach
        plain.appendLine(text)
        matches.forEach { match ->
            val minutes = match.groupValues[1].toLongOrNull() ?: 0L
            val seconds = match.groupValues[2].toLongOrNull() ?: 0L
            val fraction = match.groupValues[3]
            val millis = when (fraction.length) {
                0 -> 0L
                1 -> (fraction.toLongOrNull() ?: 0L) * 100
                2 -> (fraction.toLongOrNull() ?: 0L) * 10
                else -> fraction.take(3).toLongOrNull() ?: 0L
            }
            lines += LyricLine(minutes * 60_000 + seconds * 1_000 + millis, text)
        }
    }

    return Lyrics(
        mediaId = "",
        plainText = plain.toString().trim().takeIf { it.isNotEmpty() },
        synced = lines.sortedBy { it.timeMs },
    )
}
