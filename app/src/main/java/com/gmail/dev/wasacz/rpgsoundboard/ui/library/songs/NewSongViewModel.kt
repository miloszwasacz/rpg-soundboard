package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.DatabaseViewModel
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

    private suspend fun getSavedSongs(playListId: Long): Pair<List<Song>?, String?> = withContext(Dispatchers.IO) {
        try {
            val songs = dbViewModel.getNewSongs(playListId)
            songs to null
        } catch (e: DatabaseController.DBException) {
            null to e.code
        }
    }

    private suspend fun getSongsFromDevice(): List<TempLocalSong> = withContext(Dispatchers.IO) {
        val queryResult = arrayListOf<TempLocalSong>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
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

        val library = dbViewModel.getLocalSongsStorageIds().toSet()
        queryResult.filter { it.localStorageId !in library }
    }

    @JvmName("addSavedSongs")
    suspend fun addSongs(songs: List<Song>) = withContext(Dispatchers.IO) {
        songs.forEach {
            dbViewModel.addSongToPlaylist(it.id, playListId)
        }
    }

    @JvmName("addLocalSongs")
    suspend fun addSongs(songs: List<TempLocalSong>) = withContext(Dispatchers.IO) {
        songs.forEach {
            val id = dbViewModel.addLocalSong(it)
            dbViewModel.addSongToPlaylist(id, playListId)
        }
    }

    /**
     * Adds songs selected in file explorer *(additionally checks if a song with an uri
     * already exists - in that case adds it from the library)*.
     * @return Number of errors that occurred during the process or -1 if [uris] was empty
     */
    suspend fun addSongsFromFileExplorer(uris: List<Uri>) = withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext -1

        var failed = 0
        uris.forEach { uri ->
            val songId = dbViewModel.getSongIdByUri(uri.toString())
            songId?.let { dbViewModel.addSongToPlaylist(it, playListId) } ?: kotlin.run {
                var queryResult: TempLocalSong? = null
                //#region MediaStore query
                withContext(Dispatchers.IO) {
                    val projection = arrayOf(
                        MediaStore.Audio.Media.DISPLAY_NAME
                    )
                    getApplication<Application>().applicationContext.contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        MediaStore.Audio.Media.DISPLAY_NAME
                    )?.use {
                        val nameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                        if (it.moveToNext()) {
                            val name = it.getString(nameIndex)
                            queryResult = TempLocalSong(name, uri)
                        }
                    }
                }
                //#endregion
                queryResult?.let { addSongs(listOf(it)) } ?: failed++
            }
        }
        failed
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