package com.gmail.dev.wasacz.rpgsoundboard.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class PlaylistType {
    LOCAL,
    SPOTIFY
}

@Serializable
sealed class Playlist {
    abstract val id: Int
    abstract var name: String
    abstract val type: PlaylistType
}

@Serializable
@SerialName("playlist.local")
class LocalPlaylist(override val id: Int, override var name: String, val songList: ArrayList<LocalSong>) : Playlist() {
    @Transient
    override val type: PlaylistType = PlaylistType.LOCAL
}

@Serializable
@SerialName("playlist.spotify")
class SpotifyPlaylist(override val id: Int, override var name: String, val uri: String) : Playlist() {
    @Transient
    override val type: PlaylistType = PlaylistType.SPOTIFY
}
