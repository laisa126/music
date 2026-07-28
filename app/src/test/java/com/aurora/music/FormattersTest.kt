package com.aurora.music

import com.aurora.music.core.common.GreetingSlot
import com.aurora.music.core.common.formatDuration
import com.aurora.music.core.common.formatDurationLong
import com.aurora.music.core.common.formatFileSize
import com.aurora.music.core.common.formatRemaining
import com.aurora.music.core.common.fuzzyScore
import com.aurora.music.core.common.greetingSlot
import com.aurora.music.core.common.levenshtein
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattersTest {

    @Test
    fun `formatDuration renders minutes and seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:05", formatDuration(5_000))
        assertEquals("3:45", formatDuration(225_000))
    }

    @Test
    fun `formatDuration renders hours for long tracks`() {
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("2:05:03", formatDuration(7_503_000))
    }

    @Test
    fun `formatDuration clamps negatives`() {
        assertEquals("0:00", formatDuration(-1))
    }

    @Test
    fun `formatRemaining is negative prefixed and never underflows`() {
        assertEquals("-1:00", formatRemaining(60_000, 120_000))
        assertEquals("-0:00", formatRemaining(200_000, 120_000))
    }

    @Test
    fun `formatDurationLong reads naturally`() {
        assertEquals("1 hr 12 min", formatDurationLong(4_320_000))
        assertEquals("45 min", formatDurationLong(2_700_000))
        assertEquals("2 hr", formatDurationLong(7_200_000))
    }

    @Test
    fun `formatFileSize scales units`() {
        assertEquals("0 B", formatFileSize(0))
        assertEquals("512 B", formatFileSize(512))
        assertEquals("1.0 KB", formatFileSize(1024))
        assertEquals("5.0 MB", formatFileSize(5L * 1024 * 1024))
    }

    @Test
    fun `greeting follows time of day`() {
        assertEquals(GreetingSlot.MORNING, greetingSlot(7))
        assertEquals(GreetingSlot.AFTERNOON, greetingSlot(14))
        assertEquals(GreetingSlot.EVENING, greetingSlot(21))
        assertEquals(GreetingSlot.EVENING, greetingSlot(2))
    }

    @Test
    fun `levenshtein computes edit distance`() {
        assertEquals(0, levenshtein("abc", "abc"))
        assertEquals(3, levenshtein("", "abc"))
        assertEquals(1, levenshtein("beatles", "beatlss"))
    }

    @Test
    fun `fuzzy search tolerates typos and ranks exact matches highest`() {
        assertEquals(1f, fuzzyScore("Beatles", "beatles"), 0.001f)
        assertTrue(fuzzyScore("beat", "Beatles") > 0.9f)
        assertTrue(fuzzyScore("beatls", "Beatles") > 0.45f)
        assertTrue(fuzzyScore("zzzz", "Beatles") < 0.45f)
    }

    @Test
    fun `fuzzy search matches on word boundaries`() {
        assertTrue(fuzzyScore("moon", "Dark Side of the Moon") > 0.8f)
    }
}
