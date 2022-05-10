package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem

class PlaylistViewModel(private val dbViewModel: DatabaseViewModel, private val presetId: Long) : ListViewModel<PlaylistItem>() {
    override suspend fun getList(context: Context): List<PlaylistItem>? {
        return try {
            dbViewModel.getPlaylistsFromPreset(presetId)
        } catch (e: DatabaseController.DBException) {
            emitList(null, ListState.SERIALIZATION_ERROR, e.code)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel, private val presetId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlaylistViewModel(dbViewModel, presetId) as T
    }
}