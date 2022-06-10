package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.TempLocalSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalSongViewModel(private val dbViewModel: DatabaseViewModel) : ViewModel() {
    suspend fun getSavedSongs(playListId: Long): Pair<List<Song>?, String?> = withContext(viewModelScope.coroutineContext) {
        try {
            val songs = dbViewModel.getNewSongs(playListId)
            songs to null
        } catch (e: DatabaseController.DBException) {
            null to e.code
        }
    }

    suspend fun getSongsFromDevice(context: Context): List<TempLocalSong> {
        val queryResult = arrayListOf<TempLocalSong>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME
            )
            //TODO Filtering out already added songs
            context.applicationContext.contentResolver.query(
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

    suspend fun addSongs(songs: List<TempLocalSong>, playListId: Long) = withContext(viewModelScope.coroutineContext) {
        songs.forEach {
            val id = dbViewModel.addLocalSong(it)
            dbViewModel.addSongToPlaylist(id, playListId)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LocalSongViewModel(dbViewModel) as T
    }
}