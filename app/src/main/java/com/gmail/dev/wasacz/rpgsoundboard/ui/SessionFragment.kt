package com.gmail.dev.wasacz.rpgsoundboard.ui

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.ui.helper.MarqueeText
import com.gmail.dev.wasacz.rpgsoundboard.ui.helper.RoundIconButton
import com.gmail.dev.wasacz.rpgsoundboard.ui.theme.RPGSoundboardTheme
import com.gmail.dev.wasacz.rpgsoundboard.utils.Route
import com.gmail.dev.wasacz.rpgsoundboard.utils.viewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LibraryViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

@ExperimentalAnimationGraphicsApi
@ExperimentalMaterialApi
@Composable
fun SessionFragment(sessionState: ModalBottomSheetState) {
    //#region Helper functions
    fun isRootRoute(navController: NavController): Boolean =
        navController.currentDestination?.id == navController.graph.startDestinationId

    fun NavController.navigate(
        route: String,
        animationAtEnd: MutableState<Boolean>,
        builder: NavOptionsBuilder.() -> Unit
    ) {
        navigate(route, builder)
        animationAtEnd.value = !isRootRoute(this)
    }

    fun NavController.navigateUp(animationAtEnd: MutableState<Boolean>) {
        navigateUp()
        animationAtEnd.value = !isRootRoute(this)
    }
    //#endregion

    val viewModel: LibraryViewModel = viewModel()
    val playerViewModel: PlayerViewModel = viewModel()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    if (sessionState.currentValue == ModalBottomSheetValue.Hidden) playerViewModel.stop()

    val defaultTitle = stringResource(Route.Session.label!!)
    val title = rememberSaveable { mutableStateOf(defaultTitle) }
    val navIcon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_ic_close_to_back_arrow_white)
    val atEnd = rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopBar(title = title, navIcon = navIcon, atEnd = atEnd) {
                if (isRootRoute(navController)) {
                    playerViewModel.stop()
                    scope.launch { sessionState.hide() }
                } else navController.navigateUp(atEnd)
            }
        },
        bottomBar = {
            PlayerController(
                title = playerViewModel.currentPlaylist?.name,
                onClick = {
                    if (playerViewModel.isPlaying) playerViewModel.pause()
                    else playerViewModel.resume()
                },
                onPreviousClick = { playerViewModel.previous() },
                onNextClick = { playerViewModel.next() },
                isPlaying = playerViewModel.isPlaying,
                enabled = playerViewModel.currentPlaylist != null
            )
        }
    ) {
        viewModel.library?.let { library ->
            NavHost(
                modifier = Modifier.padding(it),
                navController = navController,
                startDestination = Route.Session.id
            ) {
                //TODO Presets
                composable(Route.Session.id) { PlaylistList(library = library, viewModel = playerViewModel) }
                composable("${Route.Session.id}/test") { Text("Test") }
                /*composable(
                    "${Route.Session.id}/{playlistId}",
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                ) { backStackEntry ->
                    SongList()
                }*/
            }
        }
    }
}

@ExperimentalAnimationGraphicsApi
@Composable
private fun TopBar(
    title: MutableState<String>,
    navIcon: AnimatedImageVector,
    atEnd: MutableState<Boolean>,
    onClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title.value) },
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(
                    painter = rememberAnimatedVectorPainter(navIcon, atEnd.value),
                    contentDescription = "Close"
                )
            }
        }
    )
}

@Composable
private fun PlayerController(
    title: String?,
    onClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPlaying: Boolean,
    enabled: Boolean
) {
    Surface(
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        elevation = AppBarDefaults.BottomAppBarElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                if (title != null) {
                    MarqueeText(
                        title,
                        edgeColor = MaterialTheme.colors.surface,
                        style = MaterialTheme.typography.h6
                    )
                }
            }
            Row(
                modifier = Modifier.weight(2f),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoundIconButton(enabled = enabled, onClick = onPreviousClick) {
                        Icon(painterResource(R.drawable.ic_skip_previous_white), null)
                    }
                    RoundIconButton(
                        backgroundColor = ButtonDefaults.buttonColors(),
                        elevation = ButtonDefaults.elevation(),
                        enabled = enabled,
                        onClick = onClick
                    ) {
                        if (isPlaying) Icon(painterResource(R.drawable.ic_pause_white), null)
                        else Icon(Icons.Rounded.PlayArrow, null)
                    }
                    RoundIconButton(enabled = enabled, onClick = onNextClick) {
                        Icon(painterResource(R.drawable.ic_skip_next_white), null)
                    }
                }
            }
            Row(modifier = Modifier.weight(1f)) {}
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun PlaylistList(library: List<Playlist>, viewModel: PlayerViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp, 8.dp)
    ) {
        items(library) { playlist ->
            var isPlaying by rememberSaveable { mutableStateOf(false) }
            PlaylistItem(playlist) {
                if (!isPlaying) viewModel.playPlaylist(it)
                else viewModel.pause()
                isPlaying = !isPlaying
            }
        }
        item {
            PlaylistItem(playlist = LocalPlaylist("bottom", arrayListOf()), onClick = {})
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun PlaylistItem(playlist: Playlist, onClick: (playlist: Playlist) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(playlist) }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp, 4.dp)
        ) {
            Text(playlist.name)
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
        }
    }
}

//#region Previews
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationGraphicsApi::class)
@Preview(showBackground = true, group = "fragment")
@Composable
fun SessionFragmentPreview() {
    RPGSoundboardTheme {
        val text = stringResource(Route.Session.label!!)
        val title = remember { mutableStateOf(text) }
        val icon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_ic_close_to_back_arrow_white)
        val atEnd = remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                TopBar(title = title, navIcon = icon, atEnd = atEnd) {
                    atEnd.value = !atEnd.value
                }
            },
            bottomBar = {
                PlayerController(
                    title = "Sample song with a very long title",
                    onClick = {},
                    onPreviousClick = {},
                    onNextClick = {},
                    isPlaying = true,
                    enabled = true
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(10) {
                    PlaylistItem(LocalPlaylist("Playlist $it", arrayListOf())) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationGraphicsApi::class)
@Preview(showBackground = true, group = "fragment", locale = "pl")
@Composable
fun SessionFragmentPreviewPL() {
    RPGSoundboardTheme {
        val text = stringResource(Route.Session.label!!)
        val title = remember { mutableStateOf(text) }
        val icon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_ic_close_to_back_arrow_white)
        val atEnd = remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                TopBar(title = title, navIcon = icon, atEnd = atEnd) {
                    atEnd.value = !atEnd.value
                }
            },
            bottomBar = {
                PlayerController(
                    title = null,
                    onClick = {},
                    onPreviousClick = {},
                    onNextClick = {},
                    isPlaying = false,
                    enabled = false
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(10) {
                    PlaylistItem(LocalPlaylist("Playlist $it", arrayListOf())) {}
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationGraphicsApi::class)
@Preview(showBackground = true, group = "fragment")
@Composable
fun SessionFragmentPreviewDark() {
    RPGSoundboardTheme(true) {
        val text = stringResource(Route.Session.label!!)
        val title = remember { mutableStateOf(text) }
        val icon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_ic_close_to_back_arrow_white)
        val atEnd = remember { mutableStateOf(false) }
        Scaffold(
            topBar = {
                TopBar(title = title, navIcon = icon, atEnd = atEnd) {
                    atEnd.value = !atEnd.value
                }
            },
            bottomBar = {
                PlayerController(
                    title = "Sample song with a very long title",
                    onClick = {},
                    onPreviousClick = {},
                    onNextClick = {},
                    isPlaying = false,
                    enabled = true
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp, 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(10) {
                    PlaylistItem(LocalPlaylist("Playlist $it", arrayListOf())) {}
                }
            }
        }
    }
}
//#endregion
