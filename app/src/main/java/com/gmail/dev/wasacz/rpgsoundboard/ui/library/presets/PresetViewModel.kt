package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset

class PresetViewModel(private val dbViewModel: DatabaseViewModel) : ListViewModel<Preset>() {
    override suspend fun getList(context: Context): List<Preset> {
        return dbViewModel.getPresets()
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PresetViewModel(dbViewModel) as T
    }
}