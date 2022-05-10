package com.gmail.dev.wasacz.rpgsoundboard.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist

class PlayerViewModel(private val dbViewModel: DatabaseViewModel) : ListViewModel<Playlist>() {
    override suspend fun getList(context: Context): List<Playlist>? {
        /*val presetId = fromExtras(extras, context.getString(R.string.nav_arg_preset_id)) ?: return null
        return try {
            dbViewModel.getPlaylistsWithSongsFromPreset(presetId)
        } catch (e: DatabaseController.DBException) {
            emitList(null, ListState.SERIALIZATION_ERROR, e.code)
            null
        }*/
        emitList(null, ListState.SERIALIZATION_ERROR)
        return null
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlayerViewModel(dbViewModel) as T
    }
}