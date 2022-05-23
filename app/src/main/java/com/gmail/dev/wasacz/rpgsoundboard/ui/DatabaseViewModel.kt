package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.DBException
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addClassicPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addPlaylistToPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deleteClassicPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deletePlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deletePreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deleteSpotifyPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistItemsFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistsFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistsNotFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPresets
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.removePlaylistFromPreset
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
     * Creates new preset.
     * @return Id of the new preset.
     */
    suspend fun createPreset(name: String): Long = db.addPreset(DBPreset(name = name))

    /**
     * Deletes preset with provided [id].
     */
    suspend fun deletePreset(id: Long) = db.deletePreset(DBPreset(id, ""))

    /**
     * Fetches all saved playlists that are not already in the preset with [presetId].
     * @return List of playlists in form of [PlaylistItems][PlaylistItem].
     */
    suspend fun getNewPlaylists(presetId: Long): List<PlaylistItem> = db.loadPlaylistsNotFromPreset(presetId)

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

    /**
     * Creates new classic playlist.
     * @return Id of the new playlist.
     */
    suspend fun createClassicPlaylist(name: String, presetId: Long): Long =
        db.addClassicPlaylist(DBPlaylist(name = name, type = DBPlaylistType.CLASSIC), presetId)

    /**
     * Adds [playlist][playlistItem] to preset with provided [presetId].
     */
    suspend fun addPlaylistToPreset(playlistItem: PlaylistItem, presetId: Long) = db.addPlaylistToPreset(playlistItem.id, presetId)

    /**
     * Removes playlist with provided [id] from preset with provided [presetId].
     */
    suspend fun removePlaylistFromPreset(id: Long, presetId: Long) = db.removePlaylistFromPreset(DBPresetPlaylistCrossRef(presetId, id))

    /**
     * Permanently deletes playlist with id from provided [playlistItem].
     */
    suspend fun deletePlaylist(playlistItem: PlaylistItem) {
        db.deletePlaylist(DBPlaylist(playlistItem.id, "", ""))
        when(playlistItem.type) {
            DBPlaylistType.PlaylistType.CLASSIC -> db.deleteClassicPlaylist(playlistItem.id)
            DBPlaylistType.PlaylistType.SPOTIFY -> db.deleteSpotifyPlaylist(DBSpotifyPlaylist(playlistItem.id, ""))
        }
    }

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
                    DBPlaylistType.PlaylistType.CLASSIC -> classicIds.add(id)
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