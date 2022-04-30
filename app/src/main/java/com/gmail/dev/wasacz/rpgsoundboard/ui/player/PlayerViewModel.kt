package com.gmail.dev.wasacz.rpgsoundboard.ui.player

import android.content.Context
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.Model
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import kotlinx.serialization.SerializationException
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalPlaylist as ModelLocalPlaylist

class PlayerViewModel : ListViewModel<Playlist>() {
    override suspend fun getList(context: Context): List<Playlist>? {
        //TODO Not yet implemented
        return try {
            Model.getSongs(context.applicationContext)
        } catch (e: SerializationException) {
            emitList(null, ListState.SERIALIZATION_ERROR)
            null
        }?.map {
            it as LocalSong
            LocalPlaylist(ModelLocalPlaylist(it.title, arrayListOf(it, it, it)))
        }
    }
}