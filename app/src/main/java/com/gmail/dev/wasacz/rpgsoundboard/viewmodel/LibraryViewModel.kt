package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

class LibraryViewModel(application: Application): AndroidViewModel(application) {
    var library by mutableStateOf<List<Playlist>?>(arrayListOf())
        private set

    init {
        viewModelScope.launch {
            fetchSongs()
        }
    }

    private fun getSongs(): List<Song>? {
        return try {
            Model.getSongs(getApplication())
        } catch (e: SerializationException) {
            null
        }
    }

    fun fetchSongs() {
        library = getSongs()?.let { songs -> List(5) { LocalPlaylist("Default playlist", ArrayList(songs as List<LocalSong>)) } }
    }

    fun saveSongs(list: List<Pair<String, String>>) {
        Model.saveSongs(getApplication(), list.map { (uri, name) -> LocalSong(name, uri) })
    }
}