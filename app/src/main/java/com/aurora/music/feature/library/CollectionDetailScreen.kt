package com.aurora.music.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.common.formatDurationLong
import com.aurora.music.core.designsystem.components.Artwork
import com.aurora.music.core.designsystem.components.LoadingState
import com.aurora.music.core.designsystem.components.SongRow
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontagePrimaryButton
import com.aurora.music.core.designsystem.montage.MontageSecondaryButton
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography
import com.aurora.music.core.designsystem.montage.MontageIcons
import com.aurora.music.feature.player.PlayerViewModel

/** Shared detail screen for albums, artists, genres, folders, playlists and "See all". */
@Composable
fun CollectionDetailScreen(
    onBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    showTrackNumbers: Boolean = false,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()
    val currentId = playerState.current?.id
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = state.title,
                navigationIcon = {
                    MontageIconButton(onClick = onBack) {
                        MontageIcon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
                },
            )
        },
        containerColor = colors.background,
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(top = padding.calculateTopPadding()))
            return@MontageScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding() + MontageSpacing.xl,
            ),
        ) {
            item(key = "header") {
                CollectionHeader(
                    title = state.title,
                    subtitle = state.subtitle,
                    artworkUri = state.artworkUri,
                    totalDurationMs = state.tracks.sumOf { it.durationMs },
                    onPlay = { playerViewModel.play(state.tracks, 0) },
                    onShuffle = { playerViewModel.shuffleAll(state.tracks) },
                )
            }

            itemsIndexed(state.tracks, key = { _, track -> track.id }) { index, track ->
                SongRow(
                    item = track,
                    isPlaying = track.id == currentId,
                    trackNumber = if (showTrackNumbers) {
                        track.trackNumber.takeIf { it > 0 } ?: (index + 1)
                    } else {
                        null
                    },
                    onClick = { playerViewModel.play(state.tracks, index) },
                    onLongClick = { viewModel.toggleFavourite(track) },
                )
            }

            if (state.tracks.isEmpty()) {
                item(key = "empty") {
                    MontageText(
                        text = "Nothing here yet.",
                        style = typography.body,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MontageSpacing.xxxl),
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionHeader(
    title: String,
    subtitle: String,
    artworkUri: String?,
    totalDurationMs: Long,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MontageSpacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Artwork(
            uri = artworkUri,
            contentDescription = title,
            shape = RoundedCornerShape(MontageSpacing.xl),
            glow = true,
            modifier = Modifier.size(190.dp),
        )
        Spacer(Modifier.height(MontageSpacing.base))
        MontageText(
            text = title,
            style = typography.heading,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(MontageSpacing.xs))
        MontageText(
            text = listOf(subtitle, formatDurationLong(totalDurationMs))
                .filter { it.isNotBlank() }
                .joinToString(" · "),
            style = typography.caption,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MontageSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(MontageSpacing.md)) {
            MontagePrimaryButton(
                onClick = onPlay,
                modifier = Modifier.weight(1f),
            ) {
                MontageIcon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = colors.textOnAccent,
                    modifier = Modifier.size(MontageIcons.medium),
                )
                Spacer(Modifier.width(MontageSpacing.sm))
                MontageText(
                    text = "Play",
                    style = typography.label,
                    color = colors.textOnAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            MontageSecondaryButton(
                onClick = onShuffle,
                modifier = Modifier.weight(1f),
            ) {
                MontageIcon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(MontageIcons.medium),
                )
                Spacer(Modifier.width(MontageSpacing.sm))
                MontageText(
                    text = "Shuffle",
                    style = typography.label,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(MontageSpacing.base))
    }
}
