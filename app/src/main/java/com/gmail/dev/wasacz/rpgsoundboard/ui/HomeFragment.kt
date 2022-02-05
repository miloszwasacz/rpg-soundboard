package com.gmail.dev.wasacz.rpgsoundboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gmail.dev.wasacz.rpgsoundboard.MainViewModel
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.Song
import com.gmail.dev.wasacz.rpgsoundboard.ui.theme.RPGSoundboardTheme
import com.gmail.dev.wasacz.rpgsoundboard.utils.SnackBar

@Composable
fun HomeFragment(scaffoldState: ScaffoldState) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val viewModel: MainViewModel = viewModel()
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                viewModel.fetchSongs()
            }) {
                Text("Refresh")
                Icon(Icons.Rounded.Refresh, contentDescription = null)
            }
        }
        viewModel.songList?.let {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp, 8.dp)
            ) {
                items(it) {
                    SongElement(song = it) { song ->
                        viewModel.playSong(song)
                    }
                }
            }
        } ?: SnackBar.Show(SnackBar.Data(stringResource(R.string.warning_corrupted_data)), scaffoldState)
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongElement(song: Song, onClick: (Song) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(song) }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp, 4.dp)
        ) {
            Text(song.name)
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SongElementPreview() {
    RPGSoundboardTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach {
                SongElement(song = LocalSong("Song_$it", "uri")) {}
            }
        }
    }
}
