package com.gmail.dev.wasacz.rpgsoundboard.model

import android.content.Context
import androidx.room.Room
import com.gmail.dev.wasacz.rpgsoundboard.model.db.*
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.*
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.ExceptionCodes.Database.getCode
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalSong
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.SpotifyPlaylist

object DatabaseController {
    class DBException(cause: Cause) : Exception() {
        val code = ExceptionCodes.getCodeString(cause.getCode())

        enum class Cause {
            MULTIPLE_PRESETS,
            INVALID_PLAYLIST_TYPE,
            INVALID_SONG_TYPE,
            INCONSISTENT_DATA
        }
    }

    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "main-database"
            ).build()
        }
        return instance!!
    }

    /**
     * Loads all presets from database and converts them to viewmodel objects.
     */
    suspend fun AppDatabase.loadPresets(): List<Preset> = presetDao().loadPresets().map {
        Preset(it.presetId, it.name)
    }

    /**
     * Adds new preset to the database.
     * @return Id of the new [preset][DBPreset].
     */
    suspend fun AppDatabase.addPreset(preset: DBPreset): Long = presetDao().insertPreset(preset)

    /**
     * Deletes preset with [preset]'s id and all its associations with playlists from database.
     */
    suspend fun AppDatabase.deletePreset(preset: DBPreset) {
        presetDao().deletePreset(preset)
        presetDao().deletePresetPlaylists(preset.presetId)
    }

    /**
     * Loads all playlists saved in the database that are not associated with preset with [presetId].
     */
    suspend fun AppDatabase.loadPlaylistsNotFromPreset(presetId: Long): List<PlaylistItem> {
        val ids = presetDao().loadPlaylistIdsFromPreset(presetId)
        return playlistDao().loadPlaylistsNotInSet(ids).map { PlaylistItem(it) }
    }

    /**
     * Loads playlists included in provided preset from database and converts them to appropriate typed viewmodel objects.
     * @throws DBException There has been a problem while loading data from database or converting it.
     * See [DBException.code] for more information about the cause.
     */
    suspend fun AppDatabase.loadPlaylistsFromPreset(presetId: Long): List<Playlist> {
        val presets = presetDao().loadPlaylistsFromPreset(presetId)
        if (presets.size != 1) throw DBException(DBException.Cause.MULTIPLE_PRESETS)
        return presets[0].playlists.map {
            try {
                when (DBPlaylistType.map(it.type)) {
                    DBPlaylistType.PlaylistType.CLASSIC -> loadClassicPlaylist(it.playlistId, it.name)
                    DBPlaylistType.PlaylistType.SPOTIFY -> loadSpotifyPlaylist(it.playlistId, it.name)
                } ?: throw DBException(DBException.Cause.INCONSISTENT_DATA)
            } catch (e: TypeCastException) {
                throw DBException(DBException.Cause.INVALID_PLAYLIST_TYPE)
            }
        }
    }

    /**
     * Loads playlist records included in provided preset from database.
     * @throws DBException There has been a problem while loading data from database or converting it.
     * See [DBException.code] for more information about the cause.
     */
    suspend fun AppDatabase.loadPlaylistItemsFromPreset(presetId: Long): List<PlaylistItem> {
        val presets = presetDao().loadPlaylistsFromPreset(presetId)
        if (presets.size != 1) throw DBException(DBException.Cause.MULTIPLE_PRESETS)
        return presets[0].playlists.map {
            try {
                PlaylistItem(it)
            } catch (e: TypeCastException) {
                throw DBException(DBException.Cause.INVALID_PLAYLIST_TYPE)
            }
        }
    }

    /**
     * Loads typed playlist based on [playlistItem]'s type.
     * @return [Playlist] or null when there is no playlist with [playlistItem]'s id.
     * @throws DBException There has been a problem while loading data from database or converting it.
     * See [DBException.code] for more information about the cause.
     */
    suspend fun AppDatabase.loadPlaylist(playlistItem: PlaylistItem): Playlist? = when (playlistItem.type) {
        DBPlaylistType.PlaylistType.CLASSIC -> loadClassicPlaylist(playlistItem)
        DBPlaylistType.PlaylistType.SPOTIFY -> loadSpotifyPlaylist(playlistItem)
    }

    /**
     * Adds new classic playlist to the database and associates it with the preset with provided [presetId].
     * @return Id of the new [playlist][DBPlaylist].
     */
    suspend fun AppDatabase.addClassicPlaylist(playlist: DBPlaylist, presetId: Long): Long {
        val id = playlistDao().insertPlaylist(playlist)
        playlistDao().insertPlaylistToPreset(DBPresetPlaylistCrossRef(presetId, id))
        return id
    }

    /**
     * Adds association between playlist with [playlistId] and preset with [presetId] to the database.
     */
    suspend fun AppDatabase.addPlaylistToPreset(playlistId: Long, presetId: Long) =
        playlistDao().insertPlaylistToPreset(DBPresetPlaylistCrossRef(presetId, playlistId))

    /**
     * Deletes association between playlist and preset with provided [ids][playlistWithPreset] from database.
     */
    suspend fun AppDatabase.removePlaylistFromPreset(playlistWithPreset: DBPresetPlaylistCrossRef) =
        playlistDao().removePlaylistFromPreset(playlistWithPreset)

    /**
     * Deletes playlist with [playlist]'s id and all it's associations with presets from the database.
     * __Remember to call type specific functions to delete the playlist from typed tables!__
     */
    suspend fun AppDatabase.deletePlaylist(playlist: DBPlaylist) {
        playlistDao().deletePlaylist(playlist)
        playlistDao().deletePlaylistFromAllPresets(playlist.playlistId)
    }

    /**
     * Deletes all associations between classic playlist with provided [id][playlistId] and songs from database.
     */
    suspend fun AppDatabase.deleteClassicPlaylist(playlistId: Long) = classicPlaylistDao().deletePlaylist(playlistId)

    /**
     * Deletes spotify playlist with [playlist]'s id from database.
     */
    suspend fun AppDatabase.deleteSpotifyPlaylist(playlist: DBSpotifyPlaylist) = spotifyPlaylistsDao().deletePlaylist(playlist)

    //#region Loading typed playlists
    private suspend fun AppDatabase.loadClassicPlaylist(playlistItem: PlaylistItem): LocalPlaylist? =
        loadClassicPlaylist(playlistItem.id, playlistItem.name)

    private suspend fun AppDatabase.loadClassicPlaylist(id: Long, name: String): LocalPlaylist? {
        val playlist = classicPlaylistDao().loadPlaylist(id) ?: return null
        val typedIdMap = mutableMapOf<SongType, ArrayList<DBSong>>()
        SongType.values().forEach { typedIdMap[it] = arrayListOf() }
        playlist.songs.forEach {
            try {
                typedIdMap[DBSongType.map(it.type)]?.add(it) ?: throw TypeCastException()
            } catch (e: TypeCastException) {
                throw DBException(DBException.Cause.INVALID_SONG_TYPE)
            }
        }
        val songs: List<Song> = typedIdMap.flatMap { (type, ids) ->
            when (type) {
                SongType.LOCAL -> loadLocalSongs(ids)
            }
        }
        return try {
            when (Playlist.mapClassicPlaylistType(songs)) {
                PlaylistType.LOCAL -> LocalPlaylist(id, name, ArrayList(songs.map { song -> song as LocalSong }))
                PlaylistType.SPOTIFY -> throw DBException(DBException.Cause.INVALID_PLAYLIST_TYPE)
            }
        } catch (e: TypeCastException) {
            throw DBException(DBException.Cause.INVALID_PLAYLIST_TYPE)
        }
    }

    private suspend fun AppDatabase.loadSpotifyPlaylist(playlistItem: PlaylistItem): SpotifyPlaylist? =
        loadSpotifyPlaylist(playlistItem.id, playlistItem.name)

    private suspend fun AppDatabase.loadSpotifyPlaylist(id: Long, name: String): SpotifyPlaylist? =
        spotifyPlaylistsDao().loadPlaylist(id)?.run {
            SpotifyPlaylist(playlistId, name, uri)
        }
    //#endregion
    //#region Loading typed songs
    private suspend fun AppDatabase.loadLocalSongs(songs: List<DBSong>): List<LocalSong> {
        val songMap = songs.associateBy { it.songId }
        return localSongDao().loadSongs(songMap.keys.toList()).map {
            val name = songMap[it.songId]?.title ?: throw DBException(DBException.Cause.INCONSISTENT_DATA)
            LocalSong(it.songId, name, it.uri)
        }
    }
    //#endregion
}
