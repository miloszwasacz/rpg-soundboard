package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.content.ContentUris
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gmail.dev.wasacz.rpgsoundboard.MainViewModel
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.ui.theme.RPGSoundboardTheme
import com.gmail.dev.wasacz.rpgsoundboard.utils.Route

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val navController = rememberNavController()
            val scaffoldState = rememberScaffoldState()
            RPGSoundboardTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    bottomBar = { NavBar(navController) },
                    floatingActionButton = {
                        StartSessionFAB {}
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    isFloatingActionButtonDocked = true
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable(Route.Home.id) { HomeFragment(scaffoldState) }
                        composable(Route.Search.id) { Content(viewModel, { playAudio(it) }) { listOf() } }
                    }
                }
            }
        }
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
fun NavBar(navController: NavController) {
    val items = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> listOf(
            Route.Home,
            Route.EMPTY,
            Route.Search
        )
        else -> listOf(
            Route.Home,
            Route.EMPTY,
            Route.EMPTY,
            Route.Search
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
                                restoreState = true
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
fun StartSessionFAB(onClick: () -> Unit) {
    Row(
        modifier = Modifier.requiredWidthIn(0.dp, dimensionResource(R.dimen.docked_fab_max_width)),
        horizontalArrangement = Arrangement.Center
    ) {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.button_start_session), textAlign = TextAlign.Center) },
            icon = { Icon(Icons.Rounded.PlayArrow, contentDescription = null) },
            onClick = { onClick() }
        )
    }
}

@Composable
fun Content(viewModel: MainViewModel, onItemClick: (String) -> Unit, onClick: () -> List<Pair<String, String>>) {
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