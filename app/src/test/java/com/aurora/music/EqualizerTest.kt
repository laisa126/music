package com.aurora.music

import com.aurora.music.data.repository.toDomain
import com.aurora.music.data.repository.toEntity
import com.aurora.music.domain.model.BuiltInPresets
import com.aurora.music.domain.model.EQ_BANDS_HZ
import com.aurora.music.domain.model.EqualizerPreset
import com.aurora.music.domain.model.formatBandLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerTest {

    @Test
    fun `there are ten bands covering 31Hz to 16kHz`() {
        assertEquals(10, EQ_BANDS_HZ.size)
        assertEquals(31, EQ_BANDS_HZ.first())
        assertEquals(16_000, EQ_BANDS_HZ.last())
    }

    @Test
    fun `band labels abbreviate kilohertz`() {
        assertEquals("31", formatBandLabel(31))
        assertEquals("1k", formatBandLabel(1_000))
        assertEquals("16k", formatBandLabel(16_000))
    }

    @Test
    fun `every built-in preset supplies one gain per band`() {
        assertTrue(BuiltInPresets.all.isNotEmpty())
        BuiltInPresets.all.forEach { preset ->
            assertEquals(
                "${preset.name} must define ${EQ_BANDS_HZ.size} gains",
                EQ_BANDS_HZ.size,
                preset.gains.size,
            )
        }
    }

    @Test
    fun `normal preset is flat and bass boost lifts the low end`() {
        val normal = BuiltInPresets.byName("Normal")
        assertNotNull(normal)
        assertTrue(normal!!.gains.all { it == 0f })

        val bass = BuiltInPresets.byName("Bass Boost")!!
        assertTrue(bass.gains.first() > 0f)
        assertTrue(bass.gains.last() <= 0f + 0.01f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a preset with the wrong number of gains is rejected`() {
        EqualizerPreset(name = "Broken", gains = listOf(1f, 2f))
    }

    @Test
    fun `preset survives a database round trip`() {
        val original = BuiltInPresets.byName("Rock")!!
        val restored = original.toEntity().toDomain()
        assertEquals(original.name, restored.name)
        assertEquals(original.gains, restored.gains)
    }

    @Test
    fun `a short gain list from an older schema is padded`() {
        val entity = com.aurora.music.data.database.entity.EqualizerPresetEntity(
            name = "Legacy",
            gains = "1,2,3",
        )
        assertEquals(EQ_BANDS_HZ.size, entity.toDomain().gains.size)
    }
}
