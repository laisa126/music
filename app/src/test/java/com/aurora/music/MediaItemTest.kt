package com.aurora.music

import com.aurora.music.data.repository.parseLrc
import com.aurora.music.domain.model.AudioFormat
import com.aurora.music.domain.model.CrossfadeDuration
import com.aurora.music.domain.model.MediaItem
import com.aurora.music.domain.model.MediaSource
import com.aurora.music.domain.model.PlaybackSpeed
import com.aurora.music.domain.model.PlayerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaItemTest {

    private fun local(uri: String? = "content://media/audio/1") = MediaItem(
        id = "1",
        title = "Track",
        artist = "Artist",
        album = "Album",
        localUri = uri,
        fileName = "track.flac",
    )

    @Test
    fun `local item resolves its file uri`() {
        assertEquals("content://media/audio/1", local().playbackUri)
        assertTrue(local().isPlayable)
    }

    @Test
    fun `item without a uri is not playable`() {
        assertFalse(local(uri = null).isPlayable)
        assertNull(local(uri = null).playbackUri)
    }

    /** Phase 2 guard: the player must resolve remote items without any UI change. */
    @Test
    fun `remote item resolves its stream url`() {
        val remote = MediaItem(
            id = "r1",
            title = "Track",
            artist = "Artist",
            album = "Album",
            source = MediaSource.REMOTE_CATALOG,
            remoteId = "cat-1",
            streamUrl = "https://example.com/stream.mp3",
        )
        assertEquals("https://example.com/stream.mp3", remote.playbackUri)
        assertTrue(remote.isPlayable)
    }

    @Test
    fun `quality badge derives from extension then mime type`() {
        assertEquals("FLAC", local().qualityBadge)
        assertTrue(local().isLossless)

        val mp3 = local().copy(fileName = "song.mp3")
        assertEquals("MP3", mp3.qualityBadge)
        assertFalse(mp3.isLossless)

        val byMime = local().copy(fileName = null, mimeType = "audio/mpeg")
        assertEquals("MP3", byMime.qualityBadge)
    }

    @Test
    fun `unknown formats degrade gracefully`() {
        val unknown = local().copy(fileName = "weird.xyz", mimeType = null)
        assertEquals(AudioFormat.UNKNOWN.label, unknown.qualityBadge)
    }

    @Test
    fun `player state computes progress and up next`() {
        val queue = (1..5).map { local().copy(id = it.toString()) }
        val state = PlayerUiState(
            current = queue[1],
            queue = queue,
            queueIndex = 1,
            positionMs = 30_000,
            durationMs = 120_000,
        )
        assertEquals(0.25f, state.progress, 0.001f)
        assertEquals(3, state.upNext.size)
        assertEquals(1, state.history.size)
    }

    @Test
    fun `progress is zero when duration unknown`() {
        assertEquals(0f, PlayerUiState(positionMs = 10, durationMs = 0).progress, 0.001f)
    }

    @Test
    fun `playback speed snaps to the nearest supported value`() {
        assertEquals(PlaybackSpeed.X1, PlaybackSpeed.nearest(1.02f))
        assertEquals(PlaybackSpeed.X1_5, PlaybackSpeed.nearest(1.48f))
        assertEquals(PlaybackSpeed.X2, PlaybackSpeed.nearest(3f))
    }

    @Test
    fun `crossfade maps from stored seconds`() {
        assertEquals(CrossfadeDuration.OFF, CrossfadeDuration.fromSeconds(0))
        assertEquals(CrossfadeDuration.FIVE, CrossfadeDuration.fromSeconds(5))
        assertEquals(CrossfadeDuration.OFF, CrossfadeDuration.fromSeconds(7))
    }

    @Test
    fun `lrc parser reads timestamps and text`() {
        val lyrics = parseLrc(
            """
            [00:12.50]First line
            [00:15.00]Second line
            [01:02.25]Third line
            """.trimIndent(),
        )
        assertEquals(3, lyrics.synced.size)
        assertEquals(12_500L, lyrics.synced[0].timeMs)
        assertEquals("First line", lyrics.synced[0].text)
        assertEquals(62_250L, lyrics.synced[2].timeMs)
        assertTrue(lyrics.isSynced)
    }

    @Test
    fun `lrc parser ignores malformed lines`() {
        val lyrics = parseLrc("no timestamp here\n[00:01.00]Real line")
        assertEquals(1, lyrics.synced.size)
    }
}
