package com.aurora.music.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.aurora.music.R

/** The five bottom-navigation tabs (spec Section 3). */
enum class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("home", R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home),
    DISCOVER("discover", R.string.nav_discover, Icons.Rounded.Explore, Icons.Outlined.Explore),
    SEARCH("search", R.string.nav_search, Icons.Rounded.Search, Icons.Outlined.Search),
    LIBRARY(
        "library",
        R.string.nav_library,
        Icons.Rounded.LibraryMusic,
        Icons.Outlined.LibraryMusic,
    ),
    SETTINGS("settings", R.string.nav_settings, Icons.Rounded.Settings, Icons.Outlined.Settings),
    ;

    companion object {
        fun fromRoute(route: String?): TopLevelDestination? =
            entries.firstOrNull { it.route == route }
    }
}

/** Detail destinations reachable from the tabs. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val PLAYER = "player"
    const val LYRICS = "lyrics"
    const val QUEUE = "queue"
    const val EQUALIZER = "equalizer"

    const val ALBUM = "album/{albumId}"
    fun album(albumId: Long) = "album/$albumId"

    const val ARTIST = "artist/{artistId}"
    fun artist(artistId: Long) = "artist/$artistId"

    const val GENRE = "genre/{genreName}"
    fun genre(name: String) = "genre/${java.net.URLEncoder.encode(name, "UTF-8")}"

    const val FOLDER = "folder/{folderPath}"
    fun folder(path: String) = "folder/${java.net.URLEncoder.encode(path, "UTF-8")}"

    const val PLAYLIST = "playlist/{playlistId}"
    fun playlist(id: Long) = "playlist/$id"

    const val SEE_ALL = "seeAll/{sectionId}"
    fun seeAll(sectionId: String) = "seeAll/$sectionId"

    const val FILE_INFO = "fileInfo/{mediaId}"
    fun fileInfo(mediaId: String) = "fileInfo/$mediaId"

    const val METADATA_EDITOR = "metadataEditor/{mediaId}"
    fun metadataEditor(mediaId: String) = "metadataEditor/$mediaId"

    const val SETTINGS_DETAIL = "settings/{categoryId}"
    fun settingsDetail(categoryId: String) = "settings/$categoryId"

    const val ABOUT = "about"
    const val CHANGELOG = "changelog"
    const val HELP = "help"
    const val BACKUP = "backup"
}
