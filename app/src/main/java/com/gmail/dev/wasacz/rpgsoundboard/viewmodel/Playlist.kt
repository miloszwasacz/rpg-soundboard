package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import io.github.esentsov.PackagePrivate
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalPlaylist as ModelLocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.Playlist as ModelPlaylist

sealed class Playlist(@PackagePrivate val playlist: ModelPlaylist) {
    val name by playlist::name
    val type by playlist::type
}

class LocalPlaylist(localPlaylist: ModelLocalPlaylist) : Playlist(localPlaylist) {
    val songList: SongList = SongList(localPlaylist.songList)
}