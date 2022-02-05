package com.gmail.dev.wasacz.rpgsoundboard.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class PlaylistType {
    MIXED
}

@Serializable
sealed class Playlist {
    abstract var name: String
    abstract val type: PlaylistType
}

@Serializable
@SerialName("playlist.mixed")
class MixedPlaylist(override var name: String, val songList: ArrayList<Song>): Playlist() {
    @Transient
    override val type: PlaylistType = PlaylistType.MIXED
}
