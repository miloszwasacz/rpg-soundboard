package com.gmail.dev.wasacz.rpgsoundboard

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.Model
import com.gmail.dev.wasacz.rpgsoundboard.model.Song
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val mediaPlayer = MediaPlayer()

    var songList by mutableStateOf<ArrayList<Song>?>(arrayListOf())
        private set

    init {
        viewModelScope.launch {
            fetchSongs()
        }
    }

    private fun getSongs(): ArrayList<Song>? {
        return try {
            Model.getSongs(getApplication())
        } catch (e: SerializationException) {
            null
        }
    }

    fun fetchSongs() {
        songList = getSongs()
    }

    fun saveSongs(list: List<Pair<String, String>>) {
        Model.saveSongs(getApplication(), list.map { (uri, name) -> LocalSong(name, uri) })
    }

    fun playSong(song: Song) {
        if(song is LocalSong) {
            mediaPlayer.apply {
                stop()
                reset()
                setDataSource(getApplication(), song.getUri())
                prepare()
                start()
            }
        }
    }

    override fun onCleared() {
        mediaPlayer.apply {
            stop()
            reset()
        }
        super.onCleared()
    }
}