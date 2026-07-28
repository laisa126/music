package com.aurora.music.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.aurora.music.core.designsystem.contourGlow
import com.aurora.music.domain.repository.ArtworkShape

/**
 * Album art with an elegant fallback. Loading is async + cached (Section 8
 * performance goals), and the placeholder never flashes a hard grey box.
 */
@Composable
fun Artwork(
    uri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glow: Boolean = false,
    crossfade: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val base = if (glow) modifier.contourGlow(shape = shape) else modifier

    Box(
        modifier = base.clip(shape).background(
            Brush.linearGradient(
                listOf(
                    scheme.surfaceContainerHighest,
                    scheme.surfaceContainer,
                ),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (uri.isNullOrBlank()) {
            ArtworkFallback()
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(uri)
                    .crossfade(crossfade)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { ArtworkFallback() },
                error = { ArtworkFallback() },
            )
        }
    }
}

@Composable
private fun ArtworkFallback() {
    Icon(
        imageVector = Icons.Rounded.MusicNote,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxSize(0.4f),
    )
}

fun ArtworkShape.toComposeShape(): Shape = when (this) {
    ArtworkShape.ROUNDED -> RoundedCornerShape(28.dp)
    ArtworkShape.SQUARE -> RoundedCornerShape(0.dp)
    ArtworkShape.CIRCLE -> CircleShape
    ArtworkShape.FULL_BLEED -> RoundedCornerShape(0.dp)
}
