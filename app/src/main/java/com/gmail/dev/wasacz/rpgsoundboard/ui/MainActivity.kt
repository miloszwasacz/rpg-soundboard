package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.content.ContentUris
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.ui.helper.ExtendedFloatingActionButton
import com.gmail.dev.wasacz.rpgsoundboard.ui.theme.RPGSoundboardTheme
import com.gmail.dev.wasacz.rpgsoundboard.utils.SnackBar
import com.gmail.dev.wasacz.rpgsoundboard.utils.changeBottom
import com.gmail.dev.wasacz.rpgsoundboard.utils.viewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LibraryViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    private fun isFabShown(backStackEntry: NavBackStackEntry?): Boolean {
        return when (backStackEntry?.destination?.route) {
            Route.Library.List -> true
            else -> false
        }
    }

    private var playerViewModel: PlayerViewModel? = null

    @OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationGraphicsApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val libraryViewModel: LibraryViewModel = viewModel()
            playerViewModel = viewModel()
            val navController = rememberNavController()
            val sessionState = rememberModalBottomSheetState(
                ModalBottomSheetValue.Hidden,
                confirmStateChange = {
                    it != ModalBottomSheetValue.HalfExpanded
                }
            )
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            val currentDestination by navController.currentBackStackEntryAsState()
            /*var fabShown by remember { mutableStateOf(false) }
            navController.addOnDestinationChangedListener { _, destination, _ ->
                scope.launch {
                    delay(1000L)
                    fabShown = isFabShown(destination)
                }
            }*/

            RPGSoundboardTheme {
                ModalBottomSheetLayout(
                    sheetState = sessionState,
                    sheetContent = {
                        if (libraryViewModel.library != null) SessionFragment(sessionState)
                        else Column(Modifier.defaultMinSize(minHeight = 1.dp)) {}
                    },
                ) {
                    val shown = isFabShown(currentDestination)
                    Scaffold(
                        scaffoldState = scaffoldState,
                        bottomBar = { NavBar(navController) },
                        floatingActionButton = {
                            val scale by animateFloatAsState(if (shown) 1f else 0f)
                            if (shown) {
                                FloatingActionButton(modifier = Modifier.scale(scale), onClick = { /*TODO*/ }) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                                }
                            }
                            /*val specFloat = spring<Float>(stiffness = Spring.StiffnessMedium)
                            val specIntSize = spring<IntSize>(stiffness = Spring.StiffnessMedium)
                            val origin = TransformOrigin(0.5f, 0.0f)
                            AnimatedVisibility(
                                visible = fabShown,//isFabShown(currentDestination),
                                enter =
                                        scaleIn(specFloat, transformOrigin = origin) +
                                        fadeIn(spring(stiffness = Spring.StiffnessHigh)) +
                                        expandIn(spring(stiffness = Spring.StiffnessHigh), expandFrom = Alignment.Center, clip = false),
                                exit =
                                        scaleOut(specFloat, transformOrigin = origin) +
                                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                        shrinkOut(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium), shrinkTowards = Alignment.Center, clip = false)
                            ) {
                                FloatingActionButton(onClick = { /*TODO*/ }) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                                }
                                /*StartSessionFAB(enabled = libraryViewModel.library != null) {
                                    playerViewModel?.setUpPlayer()
                                    scope.launch {
                                        sessionState.showFullscreen()
                                    }
                                }*/
                            }*/
                        },
                        floatingActionButtonPosition = FabPosition.Center,
                        isFloatingActionButtonDocked = true
                    ) { padding ->
                        /*Button(onClick = {
                            fabShown = !fabShown
                        }) {
                            Text("Click")
                        }
                        AnimatedVisibility(visible = fabShown) {
                            Text("test")
                        }*/
                        NavHost(
                            modifier = Modifier.padding(padding.changeBottom()),
                            navController = navController,
                            startDestination = Route.Home.id
                        ) {
                            composable(Route.Home.id) { HomeFragment() }
                            /*composable(Route.Library.id) {
                                //Content(libraryViewModel, { playAudio(it) }) { getFiles() }
                                LibraryFragment()
                            }*/
                            libraryGraph(navController)
                        }
                    }
                }
            }
            if (libraryViewModel.library == null) {
                SnackBar.Show(
                    SnackBar.Data(stringResource(R.string.warning_corrupted_data)),
                    scaffoldState
                )
            }
        }
    }

    override fun onPause() {
        playerViewModel?.pause()
        super.onPause()
    }

    private fun getFiles(): List<Pair<String, String>> {
        val results = arrayListOf<Pair<String, String>>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                results.add(contentUri.toString() to name)
            }
        }

        return results
    }

    private fun playAudio(uriString: String) {
        val uri = Uri.parse(uriString)
        MediaPlayer.create(this, uri).start()
    }
}

@Composable
private fun NavBar(navController: NavController) {
    val items = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> listOf(
            Route.Home,
            //Route.EMPTY,
            Route.Library
        )
        else -> listOf(
            Route.Home,
            //Route.EMPTY,
            //Route.EMPTY,
            Route.Library
        )
    }

    BottomAppBar(cutoutShape = CircleShape) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination
        BottomNavigation(elevation = 0.dp) {
            items.forEach { route ->
                if (route !is Route.EMPTY) {
                    BottomNavigationItem(
                        icon = {
                            route.icon?.let {
                                Icon(it, contentDescription = null)
                                it
                            } ?: route.iconId?.let {
                                Icon(painterResource(it), contentDescription = null)
                            }
                        },
                        label = {
                            route.label?.let {
                                Text(stringResource(it))
                            }
                        },
                        selected = currentRoute?.hierarchy?.any { it.route == route.id } == true,
                        onClick = {
                            navController.navigate(route.id) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    )
                } else {
                    BottomNavigationItem(
                        icon = {},
                        selected = false,
                        onClick = {},
                        enabled = false
                    )
                }
            }
        }
    }
}

@Composable
private fun StartSessionFAB(enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> Modifier
            else -> Modifier.requiredWidthIn(0.dp, dimensionResource(R.dimen.docked_fab_max_width))
        },
        horizontalArrangement = Arrangement.Center
    ) {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.button_start_session), textAlign = TextAlign.Center) },
            icon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null) },
            onClick = { onClick() },
            enabled = enabled
        )
    }
}

@Composable
private fun Content(viewModel: LibraryViewModel, onItemClick: (String) -> Unit, onClick: () -> List<Pair<String, String>>) {
    Column {
        var results by rememberSaveable { mutableStateOf(listOf<Pair<String, String>>()) }
        Text(stringResource(R.string.app_name))
        Row {
            Button(onClick = {
                results = onClick()
                viewModel.saveSongs(results)
            }) {
                Text("Query")
            }
            Button(onClick = {
                results = listOf()
            }) {
                Text("Clean")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        results.forEach { (uriString, name) ->
            Text(name, Modifier.clickable {
                onItemClick(uriString)
            })
        }
    }
}

//#region Previews
@Preview(showBackground = true, group = "scaffold")
@Composable
fun ScaffoldPreview() {
    RPGSoundboardTheme {
        Scaffold(
            bottomBar = { NavBar(rememberNavController()) },
            floatingActionButton = { StartSessionFAB {} },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Content", color = Color.Gray, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Preview(showBackground = true, group = "scaffold", locale = "pl")
@Composable
fun ScaffoldPreviewPL() {
    RPGSoundboardTheme {
        Scaffold(
            bottomBar = { NavBar(rememberNavController()) },
            floatingActionButton = { StartSessionFAB {} },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Content", color = Color.Gray, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Preview(showBackground = true, group = "scaffold")
@Composable
fun ScaffoldPreviewDark() {
    RPGSoundboardTheme(true) {
        Scaffold(
            bottomBar = { NavBar(rememberNavController()) },
            floatingActionButton = { StartSessionFAB {} },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Content", color = Color.Gray, fontStyle = FontStyle.Italic)
            }
        }
    }
}
/*
@Preview(showBackground = true)
@Composable
fun test() {
    RPGSoundboardTheme {
        var shown by remember { mutableStateOf(true) }
        Scaffold(
            bottomBar = { NavBar(rememberNavController()) },
            floatingActionButton = {
                AnimatedVisibility(visible = shown) {
                    FloatingActionButton(onClick = {}) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true
        ) {
           Button(onClick = { shown = !shown }) {
               Text("Click")
           }
        }
    }
}*/
//#endregion
