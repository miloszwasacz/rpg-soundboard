package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist

class LibraryViewModel(private val dbViewModel: DatabaseViewModel) : ListViewModel<Playlist>() {
    override suspend fun getList(context: Context, extras: Bundle?): List<Playlist>? {
        /*return try {
            Model.getSongs(context.applicationContext)
        } catch (e: SerializationException) {
            emitList(null, ListState.SERIALIZATION_ERROR)
            null
        }?.map {
            when(it.type) {
                SongType.LOCAL -> it
            }
        }*/
        //TODO Not yet implemented
        return dbViewModel.getPlaylistsWithSongsFromPreset(2)
    }

    /*fun saveSongs(context: Context) {
        //TODO Not yet implemented
        getLocalSongs(context.applicationContext).let {
            Model.saveSongs(context.applicationContext, it)
        }
    }

    private fun getLocalSongs(context: Context): ArrayList<ModelLocalSong> {
        val results = arrayListOf<ModelLocalSong>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        context.contentResolver.query(
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

                results.add(ModelLocalSong(name, contentUri.toString()))
            }
        }
        return results
    }*/

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryViewModel(dbViewModel) as T
    }
}