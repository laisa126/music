package com.aurora.music.core.common

import kotlin.math.max
import kotlin.math.min

/**
 * Similarity score in `0f..1f` used for typo-tolerant search (spec Section 6).
 *
 * Combines prefix/substring bonuses with a normalized Levenshtein distance so
 * "beatls" still finds "Beatles".
 */
fun fuzzyScore(query: String, candidate: String): Float {
    if (query.isEmpty() || candidate.isEmpty()) return 0f
    val q = query.lowercase().trim()
    val c = candidate.lowercase().trim()

    if (c == q) return 1f
    if (c.startsWith(q)) return 0.95f
    if (c.contains(q)) return 0.85f
    // Word-boundary prefix, e.g. "dsotm" style initials or "moon" in "Dark Side of the Moon".
    if (c.split(' ', '-', '_').any { it.startsWith(q) }) return 0.8f

    val distance = levenshtein(q, c.take(max(q.length * 2, 12)))
    val longest = max(q.length, min(c.length, max(q.length * 2, 12)))
    return (1f - distance.toFloat() / longest).coerceIn(0f, 1f)
}

/** Standard Levenshtein edit distance with a rolling two-row buffer. */
fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    var previous = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)

    for (i in 1..a.length) {
        current[0] = i
        for (j in 1..b.length) {
            val substitution = previous[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
            current[j] = minOf(current[j - 1] + 1, previous[j] + 1, substitution)
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[b.length]
}
