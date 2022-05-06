package com.gmail.dev.wasacz.rpgsoundboard.ui.player

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.ExceptionCodes
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist

class PlayerViewModel(private val dbViewModel: DatabaseViewModel) : ListViewModel<Playlist>() {
    override suspend fun getList(context: Context, extras: Bundle?): List<Playlist>? {
        val presetId = extras?.run {
            val res = getLong(context.getString(R.string.nav_arg_preset_id), -1)
            if (res != -1L) res else null
        } ?: kotlin.run {
            emitList(null, ListState.SERIALIZATION_ERROR, ExceptionCodes.getCodeString(ExceptionCodes.NAVIGATION_ARG_EXCEPTION))
            return null
        }
        return try {
            dbViewModel.getPlaylistsWithSongsFromPreset(presetId)
        } catch (e: DatabaseController.DBException) {
            emitList(null, ListState.SERIALIZATION_ERROR, e.code)
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val dbViewModel: DatabaseViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PlayerViewModel(dbViewModel) as T
    }
}