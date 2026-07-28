package com.aurora.music.domain.model

/** 10-band EQ centre frequencies from Section 8. */
val EQ_BANDS_HZ: List<Int> = listOf(31, 62, 125, 250, 500, 1_000, 2_000, 4_000, 8_000, 16_000)

fun formatBandLabel(hz: Int): String = if (hz >= 1_000) "${hz / 1_000}k" else "$hz"

data class EqualizerPreset(
    val id: Long = 0L,
    val name: String,
    /** dB gain per band, in the order of [EQ_BANDS_HZ]. */
    val gains: List<Float> = List(EQ_BANDS_HZ.size) { 0f },
    val isBuiltIn: Boolean = false,
) {
    init {
        require(gains.size == EQ_BANDS_HZ.size) {
            "Expected ${EQ_BANDS_HZ.size} gains, got ${gains.size}"
        }
    }
}

data class EqualizerSettings(
    val enabled: Boolean = false,
    val presetName: String = BuiltInPresets.NORMAL_NAME,
    val gains: List<Float> = List(EQ_BANDS_HZ.size) { 0f },
    val bassBoost: Float = 0f,
    val trebleBoost: Float = 0f,
    val virtualizer: Float = 0f,
    /** -1f (full left) .. 1f (full right). */
    val balance: Float = 0f,
    val preampDb: Float = 0f,
    val limiterEnabled: Boolean = true,
)

object BuiltInPresets {
    const val NORMAL_NAME = "Normal"
    const val CUSTOM_NAME = "Custom"

    val all: List<EqualizerPreset> = listOf(
        preset(NORMAL_NAME, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        preset("Pop", -1, -1, 0, 2, 4, 4, 2, 0, -1, -1),
        preset("Rock", 5, 4, 3, 1, -1, -1, 1, 3, 4, 5),
        preset("Jazz", 4, 3, 1, 2, -1, -1, 0, 1, 3, 4),
        preset("Hip Hop", 6, 5, 3, 1, -1, 0, 1, 2, 3, 3),
        preset("Dance", 6, 5, 3, 0, 0, -2, -3, -1, 2, 4),
        preset("Electronic", 5, 4, 1, 0, -2, 2, 1, 1, 4, 5),
        preset("Classical", 4, 3, 2, 1, -1, -1, 0, 2, 3, 4),
        preset("Acoustic", 4, 3, 2, 1, 1, 1, 2, 2, 3, 2),
        preset("Podcast", -2, -1, 0, 2, 4, 4, 3, 2, 0, -2),
        preset("Vocal", -2, -2, -1, 2, 4, 4, 3, 1, 0, -1),
        preset("Bass Boost", 8, 7, 5, 3, 1, 0, 0, 0, 0, 0),
        preset("Treble Boost", 0, 0, 0, 0, 0, 1, 3, 5, 7, 8),
    )

    fun byName(name: String): EqualizerPreset? = all.firstOrNull { it.name == name }

    private fun preset(name: String, vararg gains: Int) = EqualizerPreset(
        name = name,
        gains = gains.map { it.toFloat() },
        isBuiltIn = true,
    )
}
