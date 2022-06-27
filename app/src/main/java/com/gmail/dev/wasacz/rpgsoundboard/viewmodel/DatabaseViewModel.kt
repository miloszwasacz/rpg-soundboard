package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.DBException
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addClassicPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addLocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addPlaylistToPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addSong
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.addSongToPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deleteClassicPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deletePlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deletePreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deleteSong
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.deleteSpotifyPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadLocalSongIdByUri
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadLocalSongUris
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistItemsFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistsFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPlaylistsNotFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadPresets
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.loadSongsNotInPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.removePlaylistFromPreset
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.removeSongFromPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.DatabaseController.renamePreset
import com.gmail.dev.wasacz.rpgsoundboard.model.db.*

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
     * Sets name of the preset with provided [id] to [newName].
     */
    suspend fun renamePreset(id: Long, newName: String) = db.renamePreset(DBPreset(id, newName))

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
        db.addClassicPlaylist(DBPlaylist(name = name, type = DBPlaylistType.CLASSIC.name), presetId)

    /**
     * Adds [playlist][playlistItem] to preset with provided [presetId].
     */
    suspend fun addPlaylistToPreset(playlistItem: PlaylistItem, presetId: Long) = db.addPlaylistToPreset(playlistItem.id, presetId)

    /**
     * Removes playlist with provided [id] from preset with provided [presetId].
     */
    suspend fun removePlaylistFromPreset(id: Long, presetId: Long) = db.removePlaylistFromPreset(id, presetId)

    /**
     * Permanently deletes playlist with id from provided [playlistItem].
     */
    suspend fun deletePlaylist(playlistItem: PlaylistItem) {
        db.deletePlaylist(DBPlaylist(playlistItem.id, "", ""))
        when (playlistItem.type) {
            DBPlaylistType.CLASSIC -> db.deleteClassicPlaylist(playlistItem.id)
            DBPlaylistType.SPOTIFY -> db.deleteSpotifyPlaylist(DBSpotifyPlaylist(playlistItem.id, ""))
        }
    }

    /**
     * Fetches all saved songs that are not already in the playlist with [playlistId].
     * @throws DBException See [loadSongsNotInPlaylist].
     */
    suspend fun getNewSongs(playlistId: Long): List<Song> = db.loadSongsNotInPlaylist(playlistId)

    /**
     * Fetches id of a song with provided uri.
     * @return Song's [id][Song.id] if there is a song with provided [uri]; null otherwise.
     */
    suspend fun getSongIdByUri(uri: String): Long? = db.loadLocalSongIdByUri(uri) //TODO ?: spotify songs

    /**
     * Fetches uri-based ids of all saved local songs.
     */
    suspend fun getLocalSongsStorageIds(): List<Long> = db.loadLocalSongUris().mapNotNull { TempLocalSong.getIdFromUri(it) }

    /**
     * Adds song from local device storage.
     */
    suspend fun addLocalSong(song: TempLocalSong): Long {
        val id = db.addSong(song.toDBSong())
        db.addLocalSong(song.toDBLocalSong(id))
        return id
    }

    /**
     * Adds song with provided [id] to playlist with provided [playlistId].
     */
    suspend fun addSongToPlaylist(id: Long, playlistId: Long) = db.addSongToPlaylist(id, playlistId)

    /**
     * Removes song with provided [id] from playlist with provided [playlistId].
     */
    suspend fun removeSongFromPlaylist(id: Long, playlistId: Long) = db.removeSongFromPlaylist(id, playlistId)

    /**
     * Permanently deletes [song].
     */
    suspend fun deleteSong(song: Song) = db.deleteSong(DBSong(song.id, "", ""), song.type.dbType)
}