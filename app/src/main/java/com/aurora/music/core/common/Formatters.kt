package com.aurora.music.core.common

import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/** `m:ss` or `h:mm:ss`. */
fun formatDuration(millis: Long): String {
    if (millis <= 0L) return "0:00"
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

/** Remaining time rendered as `-m:ss`. */
fun formatRemaining(positionMs: Long, durationMs: Long): String =
    "-" + formatDuration((durationMs - positionMs).coerceAtLeast(0L))

/** Long form used in album/playlist headers, e.g. "1 hr 12 min". */
fun formatDurationLong(millis: Long): String {
    val totalMinutes = millis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "$hours hr $minutes min"
        hours > 0 -> "$hours hr"
        minutes > 0 -> "$minutes min"
        else -> "${(millis / 1000).coerceAtLeast(0)} sec"
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return if (unit == 0) {
        "${bytes} B"
    } else {
        String.format(Locale.US, "%.1f %s", value, units[unit])
    }
}

fun formatSampleRate(hz: Int): String =
    if (hz <= 0) "" else String.format(Locale.US, "%.1f kHz", hz / 1000.0)

fun formatBitrate(kbps: Int): String = if (kbps <= 0) "" else "$kbps kbps"

fun formatTrackCount(count: Int): String = if (count == 1) "1 song" else "$count songs"

/** Time-of-day greeting used by Home (Section 4). */
enum class GreetingSlot { MORNING, AFTERNOON, EVENING }

fun greetingSlot(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): GreetingSlot =
    when (hourOfDay) {
        in 5..11 -> GreetingSlot.MORNING
        in 12..17 -> GreetingSlot.AFTERNOON
        else -> GreetingSlot.EVENING
    }

/** "2 hours ago", "Yesterday", "3 days ago". */
fun formatRelativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    if (epochMillis <= 0L) return "Never"
    val delta = abs(now - epochMillis)
    val minutes = delta / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
        days == 1L -> "Yesterday"
        days < 30 -> "$days days ago"
        days < 365 -> "${days / 30} months ago"
        else -> "${days / 365} years ago"
    }
}
