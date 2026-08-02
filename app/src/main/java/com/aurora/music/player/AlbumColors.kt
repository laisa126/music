package com.aurora.music.player

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dominant colours extracted from album artwork for the now-playing background.
 */
data class AlbumColors(
    val primary: Color = Color(0xFF1A1A2E),
    val secondary: Color = Color(0xFF16213E),
    val surface: Color = Color(0xFF0F3460),
    val accent: Color = Color(0xFF7C6BFF),
)

/**
 * Extracts dominant colours from the given artwork URI using the Palette API.
 * Runs on [Dispatchers.IO] and caches the result for the current URI.
 */
@Composable
fun rememberAlbumColors(artworkUri: String?): AlbumColors {
    val context = LocalContext.current
    var colors by remember { mutableStateOf(AlbumColors()) }

    LaunchedEffect(artworkUri) {
        if (artworkUri.isNullOrBlank()) {
            colors = AlbumColors()
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .size(256)
                    .build()
                val result = context.imageLoader.execute(request)
                val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    ?: result.drawable?.let { drawable ->
                        val bmp = Bitmap.createBitmap(
                            drawable.intrinsicWidth.coerceAtLeast(1),
                            drawable.intrinsicHeight.coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888,
                        )
                        val canvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp
                    }

                if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    val dominant = palette.dominantSwatch
                    val vibrant = palette.vibrantSwatch
                    val muted = palette.mutedSwatch
                    val darkMuted = palette.darkMutedSwatch

                    colors = AlbumColors(
                        primary = dominant?.rgb?.let { Color(it) }
                            ?: Color(0xFF1A1A2E),
                        secondary = vibrant?.rgb?.let { Color(it) }
                            ?: darkMuted?.rgb?.let { Color(it) }
                            ?: Color(0xFF16213E),
                        surface = muted?.rgb?.let { Color(it) }
                            ?: Color(0xFF0F3460),
                        accent = vibrant?.rgb?.let { Color(it) }
                            ?: dominant?.rgb?.let { Color(it) }
                            ?: Color(0xFF7C6BFF),
                    )
                }
            }.onFailure {
                colors = AlbumColors()
            }
        }
    }

    return colors
}
