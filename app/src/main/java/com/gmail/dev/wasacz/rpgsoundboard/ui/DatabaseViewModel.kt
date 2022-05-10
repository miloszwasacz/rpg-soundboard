package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.DBException
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistItemsFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistsFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPresets
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType
import com.gmail.dev.wasacz.rpgsoundboard.model.db.*
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import kotlinx.coroutines.launch

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy { DatabaseController.getInstance(application) }

    /**
     * Fetches all [Presets][Preset].
     */
    suspend fun getPresets(): List<Preset> = db.loadPresets()

    /**
     * Fetches playlists in form of [PlaylistItems][PlaylistItem] from [Preset] with provided [presetId].
     * @throws DBException See [loadPlaylistItemsFromPreset].
     */
    suspend fun getPlaylistsFromPreset(presetId: Long): List<PlaylistItem> = db.loadPlaylistItemsFromPreset(presetId)

    /**
     * Fetches [Playlists][Playlist] from [Preset] with provided [presetId].
     * @throws DBException See [loadPlaylistsFromPreset].
     */
    suspend fun getPlaylistsWithSongsFromPreset(presetId: Long): List<Playlist> = db.loadPlaylistsFromPreset(presetId)

    /**
     * Fetches [Playlist] based on [playlistItem]'s type.
     * @return [Playlist] or null when there is no playlist with [playlistItem]'s id.
     * @throws DBException See [loadPlaylist].
     */
    suspend fun getPlaylistWithSongs(playlistItem: PlaylistItem): Playlist? = db.loadPlaylist(playlistItem)

    //TODO Not yet implemented
    fun addData() {
        viewModelScope.launch {
            val presetIds = arrayListOf<Long>()
            val presetCount = 2
            val presets = List(presetCount) { DBPreset(name = "preset${it + 1}") }.forEach {
                presetIds.add(db.presetDao().insertPreset(it))
            }

            val classicIds = arrayListOf<Long>()
            val playlistCount = 5
            val playlists = List(playlistCount) {
                val prefix: String
                val type: DBPlaylistType.PlaylistType
                if (it % 2 != 0) {
                    prefix = "s"
                    type = DBPlaylistType.PlaylistType.SPOTIFY
                } else {
                    prefix = "c"
                    type = DBPlaylistType.PlaylistType.CLASSIC
                }
                DBPlaylist(name = "${prefix}_playlist${it + 1}", type = type.value) to type
            }.forEachIndexed { i, (it, type) ->
                val id = db.playlistDao().insertPlaylist(it)
                when (type) {
                    DBPlaylistType.PlaylistType.CLASSIC -> {
                        classicIds.add(id)
                        db.classicPlaylistDao().insertPlaylist(DBClassicPlaylist(id))
                    }
                    DBPlaylistType.PlaylistType.SPOTIFY -> db.spotifyPlaylistsDao().insertPlaylist(DBSpotifyPlaylist(id, "testUri"))
                }
                db.playlistDao().insertPlaylistToPreset(DBPresetPlaylistCrossRef(presetIds[i % presetCount], id))
            }

            val songCount = 10
            val songs = List(songCount) {
                val type = SongType.LOCAL
                DBSong(title = "song${it + 1}", type = DBSongType.map(type)) to type
            }.forEachIndexed { i, (dbSong, type) ->
                val id = db.songDao().insertSongs(dbSong).first()
                when (type) {
                    SongType.LOCAL -> db.localSongDao().insertSongs(DBLocalSong(id, "testUri"))
                }
                db.songDao().insertSongToPlaylist(DBClassicPlaylistSongCrossRef(classicIds[i % classicIds.size], id))
            }
        }
    }
}