package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.model.PlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class SongViewModel(private val dbViewModel: DatabaseViewModel, private val playlistItem: PlaylistItem) : ListViewModel<Song>() {
    private val playlist = MutableStateFlow<Playlist?>(null)

    override suspend fun getList(context: Context): List<Song>? {
        delay(1000)
        return try {
            val result = dbViewModel.getPlaylistWithSongs(playlistItem)
            result?.let {
                playlist.value = it
                when (it.type) {
                    PlaylistType.LOCAL -> {
                        List<Song>(20) { _ ->
                            (it as LocalPlaylist).songList.first()
                        }
                    }
                    PlaylistType.SPOTIFY -> null
                }
            } ?: kotlin.run {
                emitList(null, ListState.EMPTY, "Playlist ot found")
                null
            }
        } catch (e: DatabaseController.DBException) {
            emitList(null, ListState.SERIALIZATION_ERROR, e.code)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel, private val playlistItem: PlaylistItem) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SongViewModel(dbViewModel, playlistItem) as T
    }
}