package com.aurora.music.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aurora.music.feature.discover.DiscoverScreen
import com.aurora.music.feature.home.HomeScreen
import com.aurora.music.feature.library.LibraryScreen
import com.aurora.music.feature.onboarding.OnboardingScreen
import com.aurora.music.feature.player.LyricsScreen
import com.aurora.music.feature.player.MiniPlayer
import com.aurora.music.feature.player.PlayerScreen
import com.aurora.music.feature.player.PlayerViewModel
import com.aurora.music.feature.player.QueueScreen
import com.aurora.music.feature.search.SearchScreen
import com.aurora.music.feature.settings.EqualizerScreen
import com.aurora.music.feature.settings.SettingsScreen

@Composable
fun AuroraApp(
    onboardingComplete: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    var showOnboarding by remember(onboardingComplete) { mutableStateOf(!onboardingComplete) }

    if (showOnboarding) {
        OnboardingScreen(onFinished = { showOnboarding = false }, modifier = modifier)
        return
    }

    MainScaffold(navController = navController, modifier = modifier)
}

@Composable
private fun MainScaffold(navController: NavHostController, modifier: Modifier = Modifier) {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = TopLevelDestination.fromRoute(currentRoute)
    val isTopLevel = topLevel != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevel,
                enter = slideInVertically(tween(220)) { it } + fadeIn(),
                exit = slideOutVertically(tween(180)) { it } + fadeOut(),
            ) {
                Column {
                    // Mini player always sits directly above the nav bar.
                    AnimatedVisibility(
                        visible = playerState.hasCurrent,
                        enter = slideInVertically(tween(240)) { it } + fadeIn(),
                        exit = slideOutVertically(tween(200)) { it } + fadeOut(),
                    ) {
                        MiniPlayer(
                            state = playerState,
                            onExpand = { navController.navigate(Routes.PLAYER) },
                            onTogglePlay = playerViewModel::togglePlayPause,
                            onNext = playerViewModel::next,
                            onToggleFavourite = { playerViewModel.toggleFavourite() },
                        )
                    }
                    AuroraNavigationBar(
                        selected = topLevel,
                        onSelect = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        val layoutDirection = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = padding.calculateStartPadding(layoutDirection),
            end = padding.calculateEndPadding(layoutDirection),
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
        )

        AuroraNavHost(
            navController = navController,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AuroraNavigationBar(
    selected: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = stringResource(destination.labelRes),
                    )
                },
                label = { Text(stringResource(destination.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun AuroraNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(180)) },
        exitTransition = { fadeOut(tween(140)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition = { fadeOut(tween(140)) },
    ) {
        composable(TopLevelDestination.HOME.route) {
            HomeScreen(
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
                onOpenEqualizer = { navController.navigate(Routes.EQUALIZER) },
                onOpenSleepTimer = { navController.navigate(Routes.PLAYER) },
                onSeeAll = { navController.navigate(Routes.seeAll(it)) },
                contentPadding = contentPadding,
            )
        }

        composable(TopLevelDestination.DISCOVER.route) {
            DiscoverScreen(
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
                onOpenGenre = { navController.navigate(Routes.genre(it)) },
                onOpenFolder = { navController.navigate(Routes.folder(it)) },
                onSeeAll = { navController.navigate(Routes.seeAll(it)) },
                contentPadding = contentPadding,
            )
        }

        composable(TopLevelDestination.SEARCH.route) {
            SearchScreen(
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
                onOpenPlaylist = { navController.navigate(Routes.playlist(it)) },
                onOpenFolder = { navController.navigate(Routes.folder(it)) },
                contentPadding = contentPadding,
            )
        }

        composable(TopLevelDestination.LIBRARY.route) {
            LibraryScreen(
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
                onOpenGenre = { navController.navigate(Routes.genre(it)) },
                onOpenFolder = { navController.navigate(Routes.folder(it)) },
                onOpenPlaylist = { navController.navigate(Routes.playlist(it)) },
                contentPadding = contentPadding,
            )
        }

        composable(TopLevelDestination.SETTINGS.route) {
            SettingsScreen(
                onOpenEqualizer = { navController.navigate(Routes.EQUALIZER) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                contentPadding = contentPadding,
            )
        }

        composable(
            route = Routes.PLAYER,
            enterTransition = { slideInVertically(tween(300)) { it } },
            exitTransition = { fadeOut(tween(150)) },
            popExitTransition = { slideOutVertically(tween(260)) { it } },
        ) {
            PlayerScreen(
                onCollapse = { navController.popBackStack() },
                onOpenQueue = { navController.navigate(Routes.QUEUE) },
                onOpenLyrics = { navController.navigate(Routes.LYRICS) },
                onOpenEqualizer = { navController.navigate(Routes.EQUALIZER) },
                onOpenSleepTimer = { navController.navigate(Routes.EQUALIZER) },
            )
        }

        composable(Routes.QUEUE) {
            QueueScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.LYRICS) {
            LyricsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.EQUALIZER) {
            EqualizerScreen(onBack = { navController.popBackStack() })
        }

        detailDestinations(navController, contentPadding)
    }
}
