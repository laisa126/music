package com.aurora.music.data.mediastore

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.aurora.music.data.database.entity.SongEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the device's audio library via MediaStore.
 *
 * Deliberately returns [SongEntity] rows rather than domain models so the
 * repository stays the single place that maps storage -> domain.
 */
@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            @Suppress("DEPRECATION")
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    /**
     * @param minDurationSeconds tracks shorter than this are treated as ringtones/notifications.
     * @param excludedFolders absolute folder paths the user opted out of.
     * @param onProgress invoked as `(scanned, total)`.
     */
    fun scan(
        minDurationSeconds: Int = 20,
        excludedFolders: Set<String> = emptySet(),
        includedFolders: Set<String> = emptySet(),
        onProgress: (scanned: Int, total: Int) -> Unit = { _, _ -> },
    ): List<SongEntity> {
        if (!hasAudioPermission()) return emptyList()

        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = buildProjection()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val now = System.currentTimeMillis()
        val results = ArrayList<SongEntity>(512)

        context.contentResolver.query(collection, projection, selection, null, sortOrder)
            ?.use { cursor ->
                val total = cursor.count
                val cols = ColumnIndices(cursor)
                var scanned = 0
                while (cursor.moveToNext()) {
                    scanned++
                    if (scanned % 100 == 0 || scanned == total) onProgress(scanned, total)

                    val entity = cursor.toSongEntity(cols, now) ?: continue
                    if (entity.durationMs < minDurationSeconds * 1_000L) continue

                    val folder = entity.folderPath
                    if (folder != null) {
                        if (excludedFolders.any { folder.startsWith(it) }) continue
                        if (includedFolders.isNotEmpty() &&
                            includedFolders.none { folder.startsWith(it) }
                        ) {
                            continue
                        }
                    }
                    results += entity
                }
                onProgress(total, total)
            }

        return results
    }

    private fun buildProjection(): Array<String> = buildList {
        add(MediaStore.Audio.Media._ID)
        add(MediaStore.Audio.Media.TITLE)
        add(MediaStore.Audio.Media.ARTIST)
        add(MediaStore.Audio.Media.ALBUM)
        add(MediaStore.Audio.Media.ALBUM_ARTIST)
        add(MediaStore.Audio.Media.COMPOSER)
        add(MediaStore.Audio.Media.DURATION)
        add(MediaStore.Audio.Media.TRACK)
        add(MediaStore.Audio.Media.YEAR)
        add(MediaStore.Audio.Media.ALBUM_ID)
        add(MediaStore.Audio.Media.ARTIST_ID)
        add(MediaStore.Audio.Media.DATA)
        add(MediaStore.Audio.Media.DISPLAY_NAME)
        add(MediaStore.Audio.Media.SIZE)
        add(MediaStore.Audio.Media.MIME_TYPE)
        add(MediaStore.Audio.Media.DATE_ADDED)
        add(MediaStore.Audio.Media.DATE_MODIFIED)
        add(MediaStore.Audio.Media.GENRE)
        add(MediaStore.Audio.Media.BITRATE)
        // DISC_NUMBER exists since API 31; minSdk is 33 so no guard is needed.
        add(MediaStore.Audio.Media.DISC_NUMBER)
    }.toTypedArray()

    private class ColumnIndices(cursor: Cursor) {
        val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val albumArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
        val composer = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
        val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val track = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
        val disc = cursor.getColumnIndex(MediaStore.Audio.Media.DISC_NUMBER)
        val year = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
        val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val artistId = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
        val data = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val displayName = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
        val size = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        val mime = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
        val dateAdded = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
        val dateModified = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
        val genre = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
        val bitrate = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
    }

    private fun Cursor.toSongEntity(c: ColumnIndices, scanStamp: Long): SongEntity? {
        val mediaId = runCatching { getLong(c.id) }.getOrNull() ?: return null
        val contentUri: Uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            mediaId,
        )
        val albumId = optLong(c.albumId)
        val path = optString(c.data)
        val rawTrack = optInt(c.track)

        return SongEntity(
            id = mediaId.toString(),
            title = optString(c.title)?.takeIf { it.isNotBlank() }
                ?: optString(c.displayName).orEmpty().substringBeforeLast('.'),
            artist = optString(c.artist)
                ?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                ?: "Unknown artist",
            album = optString(c.album)
                ?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                ?: "Unknown album",
            albumArtist = optString(c.albumArtist),
            composer = optString(c.composer),
            genre = optString(c.genre),
            durationMs = optLong(c.duration),
            // MediaStore encodes TRACK as disc*1000 + track for multi-disc albums.
            trackNumber = if (rawTrack > 1000) rawTrack % 1000 else rawTrack,
            discNumber = if (rawTrack > 1000) rawTrack / 1000 else optInt(c.disc),
            year = optInt(c.year),
            source = "LOCAL",
            localUri = contentUri.toString(),
            artworkUri = if (albumId > 0) albumArtUri(albumId) else null,
            albumId = albumId,
            artistId = optLong(c.artistId),
            filePath = path,
            fileName = optString(c.displayName) ?: path?.substringAfterLast('/'),
            fileSizeBytes = optLong(c.size),
            mimeType = optString(c.mime),
            bitrateKbps = (optInt(c.bitrate) / 1000).coerceAtLeast(0),
            dateAdded = optLong(c.dateAdded),
            dateModified = optLong(c.dateModified),
            folderPath = path?.let { File(it).parent },
            lastSeen = scanStamp,
        )
    }

    private fun Cursor.optString(index: Int): String? =
        if (index >= 0 && !isNull(index)) getString(index) else null

    private fun Cursor.optLong(index: Int): Long =
        if (index >= 0 && !isNull(index)) getLong(index) else 0L

    private fun Cursor.optInt(index: Int): Int =
        if (index >= 0 && !isNull(index)) getInt(index) else 0

    companion object {
        fun albumArtUri(albumId: Long): String =
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId,
            ).toString()
    }
}
