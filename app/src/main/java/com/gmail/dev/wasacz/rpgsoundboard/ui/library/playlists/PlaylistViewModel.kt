package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBPlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import kotlinx.coroutines.withContext

class PlaylistViewModel(private val dbViewModel: DatabaseViewModel, private val presetId: Long) : ListViewModel<PlaylistItem>() {
    override suspend fun getList(context: Context): List<PlaylistItem>? {
        return try {
            dbViewModel.getPlaylistsFromPreset(presetId)
        } catch (e: DatabaseController.DBException) {
            emitList(null, ListState.SERIALIZATION_ERROR, e.code)
            null
        }
    }

    suspend fun renamePreset(newName: String) = withContext(viewModelScope.coroutineContext) {
        dbViewModel.renamePreset(presetId, newName)
    }

    suspend fun createPlaylist(name: String): PlaylistItem = withContext(viewModelScope.coroutineContext) {
        val id = dbViewModel.createClassicPlaylist(name, presetId)
        PlaylistItem(id, name, DBPlaylistType.PlaylistType.CLASSIC)
    }

    suspend fun getAllPlaylists(): List<PlaylistItem> = withContext(viewModelScope.coroutineContext) {
        dbViewModel.getNewPlaylists(presetId)
    }

    suspend fun addPlaylists(playlists: List<PlaylistItem>) = withContext(viewModelScope.coroutineContext) {
        for (playlist in playlists)
            dbViewModel.addPlaylistToPreset(playlist, presetId)
    }

    suspend fun removePlaylists(playlists: List<PlaylistItem>) = withContext(viewModelScope.coroutineContext) {
        for (playlist in playlists)
            dbViewModel.removePlaylistFromPreset(playlist.id, presetId)
    }

    suspend fun deletePlaylists(playlists: List<PlaylistItem>) = withContext(viewModelScope.coroutineContext) {
        for (playlist in playlists)
            dbViewModel.deletePlaylist(playlist)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel, private val presetId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlaylistViewModel(dbViewModel, presetId) as T
    }
}