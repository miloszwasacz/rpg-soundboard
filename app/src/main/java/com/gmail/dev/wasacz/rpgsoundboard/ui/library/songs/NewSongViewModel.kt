package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.app.Application
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.TempLocalSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewSongViewModel(
    application: Application,
    private val dbViewModel: DatabaseViewModel,
    private val playListId: Long
) : AndroidViewModel(application) {
    private val _savedSongs = MutableStateFlow<List<Song>?>(null)
    val savedSongs by flowLazy(_savedSongs) {
        it.emit(getSavedSongs(playListId).first)
    }

    private val _localSongs = MutableStateFlow<List<TempLocalSong>?>(null)
    val localSongs by flowLazy(_localSongs) {
        it.emit(getSongsFromDevice())
    }

    private suspend fun getSavedSongs(playListId: Long): Pair<List<Song>?, String?> = withContext(viewModelScope.coroutineContext) {
        try {
            val songs = dbViewModel.getNewSongs(playListId)
            songs to null
        } catch (e: DatabaseController.DBException) {
            null to e.code
        }
    }

    private suspend fun getSongsFromDevice(): List<TempLocalSong> {
        val queryResult = arrayListOf<TempLocalSong>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME
            )
            //TODO Filtering out already added songs
            getApplication<Application>().applicationContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.DISPLAY_NAME
            )?.use {
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                while (it.moveToNext()) {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    queryResult.add(TempLocalSong(id, name))
                }
            }
        }

        return queryResult
    }

    @JvmName("addSavedSongs")
    suspend fun addSongs(songs: List<Song>) = withContext(viewModelScope.coroutineContext) {
        songs.forEach {
            dbViewModel.addSongToPlaylist(it.id, playListId)
        }
    }

    @JvmName("addLocalSongs")
    suspend fun addSongs(songs: List<TempLocalSong>) = withContext(viewModelScope.coroutineContext) {
        songs.forEach {
            val id = dbViewModel.addLocalSong(it)
            dbViewModel.addSongToPlaylist(id, playListId)
        }
    }

    private fun <T : Any> flowLazy(
        flow: MutableStateFlow<T?>,
        initialEmit: suspend CoroutineScope.(flow: MutableStateFlow<T?>) -> Unit
    ): Lazy<StateFlow<T?>> = lazy {
        viewModelScope.launch {
            initialEmit(flow)
        }
        flow
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val dbViewModel: DatabaseViewModel,
        private val playListId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NewSongViewModel(application, dbViewModel, playListId) as T
    }
}