package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.os.Parcelable
import com.gmail.dev.wasacz.rpgsoundboard.model.PlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBPlaylistType
import kotlinx.parcelize.Parcelize

/*sealed class Playlist(@PackagePrivate val playlist: ModelPlaylist) {
    val name by playlist::name
    val type by playlist::type

    fun ModelPlaylist.getTypedPlaylist(): Playlist = when(type) {
        PlaylistType.LOCAL -> LocalPlaylist(this as ModelLocalPlaylist)
        PlaylistType.SPOTIFY -> SpotifyPlaylist(this as ModelSpotifyPlaylist)
    }
}

class LocalPlaylist(localPlaylist: ModelLocalPlaylist) : Playlist(localPlaylist) {
    val songList: SongList = SongList(localPlaylist.songList)
}

class SpotifyPlaylist(spotifyPlaylist: ModelSpotifyPlaylist) : Playlist(spotifyPlaylist) {
    val uri by spotifyPlaylist::uri
}*/

sealed class Playlist {
    abstract val id: Long
    abstract var name: String
    abstract val type: PlaylistType

    companion object {
        /**
         * Returns playlist type based on contents of song list.
         * @throws TypeCastException No appropriate enum type exists for provided song list
         */
        fun mapClassicPlaylistType(songs: List<Song>): PlaylistType = when {
            songs.all { it.type == SongType.LOCAL } -> PlaylistType.LOCAL
            else -> throw TypeCastException()
        }
    }
}

@Parcelize
class PlaylistItem(val id: Long, val name: String, val type: DBPlaylistType) : Parcelable {
    constructor(dbPlaylist: DBPlaylist) : this(
        dbPlaylist.playlistId,
        dbPlaylist.name,
        DBPlaylistType.map(dbPlaylist.type)
    )
}

class LocalPlaylist(override val id: Long, override var name: String, val songList: ArrayList<LocalSong>) : Playlist() {
    override val type: PlaylistType = PlaylistType.LOCAL
}

class SpotifyPlaylist(override val id: Long, override var name: String, val uri: String) : Playlist() {
    override val type: PlaylistType = PlaylistType.SPOTIFY
}