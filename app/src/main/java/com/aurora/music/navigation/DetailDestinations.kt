package com.aurora.music.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aurora.music.feature.library.CollectionDetailScreen
import com.aurora.music.feature.settings.AboutScreen

/** Album / artist / genre / folder / playlist / see-all / about destinations. */
fun NavGraphBuilder.detailDestinations(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    val back: () -> Unit = { navController.popBackStack() }

    composable(
        route = Routes.ALBUM,
        arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
    ) {
        CollectionDetailScreen(
            onBack = back,
            contentPadding = contentPadding,
            showTrackNumbers = true,
        )
    }

    composable(
        route = Routes.ARTIST,
        arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
    ) {
        CollectionDetailScreen(onBack = back, contentPadding = contentPadding)
    }

    composable(
        route = Routes.GENRE,
        arguments = listOf(navArgument("genreName") { type = NavType.StringType }),
    ) {
        CollectionDetailScreen(onBack = back, contentPadding = contentPadding)
    }

    composable(
        route = Routes.FOLDER,
        arguments = listOf(navArgument("folderPath") { type = NavType.StringType }),
    ) {
        CollectionDetailScreen(onBack = back, contentPadding = contentPadding)
    }

    composable(
        route = Routes.PLAYLIST,
        arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
    ) {
        CollectionDetailScreen(
            onBack = back,
            contentPadding = contentPadding,
            showTrackNumbers = true,
        )
    }

    composable(
        route = Routes.SEE_ALL,
        arguments = listOf(navArgument("sectionId") { type = NavType.StringType }),
    ) {
        CollectionDetailScreen(onBack = back, contentPadding = contentPadding)
    }

    composable(Routes.ABOUT) {
        AboutScreen(onBack = back)
    }
}
