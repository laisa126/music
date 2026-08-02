package com.aurora.music.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aurora.music.feature.library.CollectionDetailScreen
import com.aurora.music.feature.library.FileInfoScreen
import com.aurora.music.feature.library.MetadataEditorScreen
import com.aurora.music.feature.settings.AboutScreen
import com.aurora.music.feature.settings.BackupScreen

/** Album / artist / genre / folder / playlist / see-all / about / info / metadata / backup destinations. */
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

    composable(
        route = Routes.FILE_INFO,
        arguments = listOf(navArgument("mediaId") { type = NavType.StringType }),
    ) {
        FileInfoScreen(onBack = back)
    }

    composable(
        route = Routes.METADATA_EDITOR,
        arguments = listOf(navArgument("mediaId") { type = NavType.StringType }),
    ) {
        MetadataEditorScreen(onBack = back)
    }

    composable(Routes.ABOUT) {
        AboutScreen(onBack = back)
    }

    composable(Routes.BACKUP) {
        BackupScreen(onBack = back)
    }
}
