package com.gmail.dev.wasacz.rpgsoundboard.utils

import androidx.annotation.StringRes
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import com.gmail.dev.wasacz.rpgsoundboard.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class Route(val id: String, @StringRes val label: Int?, val icon: ImageVector?) {
    object Home: Route("home", R.string.route_home, Icons.Rounded.Home)
    object Search: Route("search", R.string.route_search, Icons.Rounded.Search)
    object EMPTY: Route("", null, null)
}

sealed class SnackBar {
    data class Data(val message: String, val action: Action? = null, val duration: SnackbarDuration = SnackbarDuration.Short)
    data class Action(val label: String, val actionPerformed: () -> Unit, val actionDismissed: () -> Unit)

    companion object {
        private suspend fun showSnackBar(snackBar: Data, scaffoldState: ScaffoldState) {
            snackBar.apply {
                when (scaffoldState.snackbarHostState.showSnackbar(message, action?.label)) {
                    SnackbarResult.ActionPerformed -> {
                        action?.let { it.actionPerformed() }
                    }
                    SnackbarResult.Dismissed -> {
                        action?.let { it.actionDismissed() }
                    }
                }
            }
        }

        fun show(snackBarData: Data, scaffoldState: ScaffoldState, scope: CoroutineScope) {
            scope.launch {
                showSnackBar(snackBarData, scaffoldState)
            }
        }

        @Composable
        fun Show(snackBarData: Data, scaffoldState: ScaffoldState) {
            return LaunchedEffect(scaffoldState.snackbarHostState) {
                showSnackBar(snackBarData, scaffoldState)
            }
        }
    }
}
