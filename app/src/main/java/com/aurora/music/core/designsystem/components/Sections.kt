package com.aurora.music.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.music.core.common.formatTrackCount
import com.aurora.music.core.designsystem.montage.MontageChip
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageShapes
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.domain.model.Album
import com.aurora.music.domain.model.Artist
import com.aurora.music.domain.model.MediaItem

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onSeeAll: (() -> Unit)? = null,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Row(
        modifier = modifier.fillMaxWidth().padding(start = MontageSpacing.screenHorizontal, end = MontageSpacing.sm, top = MontageSpacing.sectionGap, bottom = MontageSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MontageText(text = title, style = typography.heading, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            if (subtitle != null) { MontageText(text = subtitle, style = typography.caption, color = colors.textSecondary) }
        }
        if (onSeeAll != null) { MontageChip(label = "See all", selected = false, onClick = onSeeAll) }
    }
}

@Composable
fun SongCarousel(items: List<MediaItem>, onItemClick: (Int) -> Unit, modifier: Modifier = Modifier, cardSize: androidx.compose.ui.unit.Dp = 150.dp) {
    if (items.isEmpty()) return
    LazyRow(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            MediaCard(title = item.title, subtitle = item.artist, artworkUri = item.artworkUri, size = cardSize, onClick = { onItemClick(index) })
        }
    }
}

@Composable
fun AlbumCarousel(albums: List<Album>, onAlbumClick: (Album) -> Unit, modifier: Modifier = Modifier) {
    if (albums.isEmpty()) return
    LazyRow(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
        items(albums, key = { it.id }) { album ->
            MediaCard(title = album.title, subtitle = album.artist, artworkUri = album.artworkUri, onClick = { onAlbumClick(album) })
        }
    }
}

@Composable
fun ArtistCarousel(artists: List<Artist>, onArtistClick: (Artist) -> Unit, modifier: Modifier = Modifier) {
    if (artists.isEmpty()) return
    LazyRow(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = MontageSpacing.screenHorizontal), horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
        items(artists, key = { it.id }) { artist ->
            MediaCard(title = artist.name, subtitle = formatTrackCount(artist.trackCount), artworkUri = artist.artworkUri, shape = CircleShape, onClick = { onArtistClick(artist) })
        }
    }
}

@Composable
fun MediaCard(
    title: String, subtitle: String, artworkUri: String?, onClick: () -> Unit,
    modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 150.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(MontageShapes.card),
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(modifier = modifier.width(size).clickable(onClick = onClick)) {
        Artwork(uri = artworkUri, contentDescription = title, shape = shape, glow = true, modifier = Modifier.size(size))
        Spacer(Modifier.height(MontageSpacing.sm))
        MontageText(text = title, style = typography.caption, color = colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (shape == CircleShape) TextAlign.Center else TextAlign.Start, modifier = Modifier.fillMaxWidth())
        MontageText(text = subtitle, style = typography.mini, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (shape == CircleShape) TextAlign.Center else TextAlign.Start, modifier = Modifier.fillMaxWidth())
    }
}
