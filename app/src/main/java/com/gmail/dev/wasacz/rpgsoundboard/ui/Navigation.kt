package com.gmail.dev.wasacz.rpgsoundboard.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.gmail.dev.wasacz.rpgsoundboard.R

sealed class Route(val id: String, @StringRes val label: Int?, val icon: ImageVector? = null, @DrawableRes val iconId: Int? = null) {
    object Home : Route("home", R.string.route_home, Icons.Rounded.Home)
    object Library : Route("library", R.string.route_library, iconId = R.drawable.ic_library_black) {
        const val List = "session_list"
        const val playlistArg = "playlistName"
    }
    object Session : Route("session", R.string.route_session) {
        const val List = "library_list"
    }
    object EMPTY : Route("", null)
}

fun NavGraphBuilder.sessionGraph(navController: NavController) {
    navigation(Route.Session.List, Route.Session.id) {
        composable(Route.Session.List) {}
        //TODO Presets
    }
}

fun NavGraphBuilder.libraryGraph(navController: NavController) {
    navigation(Route.Library.List, Route.Library.id) {
        composable(Route.Library.List) { LibraryFragment(navController) }
        composable(
            "${Route.Library.List}/{${Route.Library.playlistArg}}",
            listOf(navArgument(Route.Library.playlistArg) { type = NavType.StringType })
        ) { entry ->
            PlaylistFragment(entry.arguments?.getString(Route.Library.playlistArg), navController)
        }
    }
}