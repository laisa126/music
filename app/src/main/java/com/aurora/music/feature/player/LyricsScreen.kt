package com.aurora.music.feature.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.music.core.designsystem.components.AuroraEmptyState
import com.aurora.music.core.designsystem.montage.MontageAppBar
import com.aurora.music.core.designsystem.montage.MontageIconButton
import com.aurora.music.core.designsystem.montage.MontageIcon
import com.aurora.music.core.designsystem.montage.MontageScaffold
import com.aurora.music.core.designsystem.montage.MontageSpacing
import com.aurora.music.core.designsystem.montage.MontageText
import com.aurora.music.core.designsystem.montage.MontageTheme
import com.aurora.music.core.designsystem.montage.MontageTypography

/** Full-screen lyrics view, synced when an `.lrc` or embedded timing exists. */
@Composable
fun LyricsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val colors = MontageTheme.colors
    val typography = MontageTheme.typography

    val activeLine = lyrics?.synced?.indexOfLast {
        it.timeMs + (lyrics?.offsetMs ?: 0L) <= state.positionMs
    } ?: -1

    LaunchedEffect(activeLine) {
        if (activeLine >= 0) {
            runCatching { listState.animateScrollToItem((activeLine - 2).coerceAtLeast(0)) }
        }
    }

    MontageScaffold(
        modifier = modifier,
        topBar = {
            MontageAppBar(
                title = state.current?.title ?: "Lyrics",
                navigationIcon = {
                    MontageIconButton(onClick = onBack) {
                        MontageIcon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                        )
                    }
                },
                actions = {
                    if (state.current?.artist != null) {
                        MontageText(
                            text = state.current!!.artist,
                            style = typography.caption,
                            color = colors.textSecondary,
                        )
                    }
                },
            )
        },
        containerColor = colors.background,
    ) { padding ->
        val current = lyrics

        when {
            current == null || current.isEmpty -> AuroraEmptyState(
                icon = Icons.Rounded.Lyrics,
                title = "No lyrics found",
                message = "Add an .lrc file next to this track, or paste lyrics from the " +
                    "metadata editor.",
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
            )

            current.isSynced -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(horizontal = MontageSpacing.xl, vertical = 40.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(
                    items = current.synced,
                    key = { index, line -> "${index}_${line.timeMs}" },
                ) { index, line ->
                    val isActive = index == activeLine
                    val color by animateColorAsState(
                        targetValue = if (isActive) {
                            colors.accent
                        } else {
                            colors.textSecondary.copy(alpha = 0.55f)
                        },
                        label = "lyricLineColor",
                    )
                    MontageText(
                        text = line.text,
                        style = if (isActive) typography.heading else typography.body,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(top = padding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MontageSpacing.xl, vertical = MontageSpacing.xxl),
            ) {
                MontageText(
                    text = current.plainText.orEmpty(),
                    style = typography.body,
                    color = colors.textPrimary,
                )
            }
        }
    }
}
