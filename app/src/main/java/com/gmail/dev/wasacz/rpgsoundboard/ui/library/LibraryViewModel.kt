package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.gmail.dev.wasacz.rpgsoundboard.model.Model
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import kotlinx.serialization.SerializationException
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong as ModelLocalSong

class LibraryViewModel : ListViewModel<Song>() {
    override suspend fun getList(context: Context): List<Song>? {
        return try {
            Model.getSongs(context.applicationContext)
        } catch (e: SerializationException) {
            emitList(null, ListState.SERIALIZATION_ERROR)
            null
        }?.map {
            when(it.type) {
                SongType.LOCAL -> LocalSong(it as ModelLocalSong)
            }
        }
    }

    fun saveSongs(context: Context) {
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
    }
}