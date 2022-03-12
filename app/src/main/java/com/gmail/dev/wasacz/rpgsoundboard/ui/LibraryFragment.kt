package com.gmail.dev.wasacz.rpgsoundboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.ui.theme.RPGSoundboardTheme
import com.gmail.dev.wasacz.rpgsoundboard.utils.viewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LibraryViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryFragment() {
    val viewModel: LibraryViewModel = viewModel()

    viewModel.library?.let {
        val itemSize = dimensionResource(R.dimen.max_image_size) + dimensionResource(R.dimen.default_padding).times(2)
        LazyVerticalGrid(
            cells = GridCells.Adaptive(itemSize),
            contentPadding = PaddingValues(dimensionResource(R.dimen.default_padding))
        ) {
            items(it) { playlist ->
                Row(
                    modifier = Modifier.padding(16.dp, 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PlaylistItem(playlist)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PlaylistItem(playlist: Playlist) {
    val padding = dimensionResource(R.dimen.default_padding)
    val maxImageSize = dimensionResource(R.dimen.max_image_size)
    Card(
        modifier = Modifier.widthIn(max = maxImageSize + padding.times(2)),
        elevation = dimensionResource(R.dimen.default_card_elevation),
        onClick = {}
    ) {
        Column(Modifier.background(MaterialTheme.colors.onBackground.copy(alpha = 0.25f))) {
            Image(
                modifier = Modifier
                    .padding(padding)
                    .heightIn(max = maxImageSize)
                    .fillMaxHeight(),
                painter = painterResource(R.drawable.ic_album_black),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground.copy(alpha = 0.25f))
            )
            Surface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(padding, padding / 2)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.h5,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlaylistItemEdit(playlist: MutableState<Playlist?>) {
    fun closeDialog(state: MutableState<Playlist?>) {
        state.value = null
    }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { closeDialog(playlist) }
    ) {
        Surface(Modifier.fillMaxSize()) {

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistItemPreview() {
    RPGSoundboardTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                PlaylistItem(LocalPlaylist("Title", arrayListOf()))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistItemPreviewDark() {
    RPGSoundboardTheme(true) {
        Surface {
            Column(Modifier.padding(16.dp)) {
                PlaylistItem(LocalPlaylist("Title", arrayListOf()))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
private fun FragmentPreview() {
    RPGSoundboardTheme {
        Scaffold {
            LazyVerticalGrid(cells = GridCells.Adaptive(160.dp), contentPadding = PaddingValues(8.dp)) {
                items(10) { i ->
                    Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.Center) {
                        PlaylistItem(LocalPlaylist("Playlist $i", arrayListOf()))
                    }
                }
            }
        }
    }
}