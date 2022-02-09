package com.gmail.dev.wasacz.rpgsoundboard.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class PlaylistType {
    LOCAL
}

@Serializable
sealed class Playlist {
    abstract var name: String
    abstract val type: PlaylistType
}

@Serializable
@SerialName("playlist.local")
class LocalPlaylist(override var name: String, val songList: ArrayList<LocalSong>): Playlist() {
    @Transient
    override val type: PlaylistType = PlaylistType.LOCAL
}
