package com.gmail.dev.wasacz.rpgsoundboard.utils

import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.gmail.dev.wasacz.rpgsoundboard.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

//#region Custom views
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

@Composable
fun BottomAppBarSpacer() {
    BottomAppBar(Modifier.alpha(0f)) {
        BottomNavigation(elevation = 0.dp) {
            BottomNavigationItem(
                icon = { Icon(Icons.Rounded.Home, null) },
                label = { Text("Spacer") },
                selected = false,
                onClick = {}
            )
        }
    }
}
@Composable
fun PaddingValues.changeBottom(newValue: Dp = 0.dp): PaddingValues {
    val layoutDir = when(LocalConfiguration.current.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }
    return PaddingValues(
        calculateStartPadding(layoutDir),
        calculateTopPadding(),
        calculateEndPadding(layoutDir),
        newValue
    )
}
//#endregion

//#region Misc
@Composable
inline fun <reified VM : ViewModel> viewModel() = composeViewModel<VM>(LocalContext.current as ComponentActivity)

@ExperimentalMaterialApi
suspend fun ModalBottomSheetState.showFullscreen() {
    animateTo(ModalBottomSheetValue.Expanded)
}
//#endregion
