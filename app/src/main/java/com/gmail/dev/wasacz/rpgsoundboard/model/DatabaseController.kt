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
        val code by lazy { ExceptionCodes.getCodeString(cause.getCode()) }

        enum class Cause(val id: Int) {
            MULTIPLE_PRESETS(1),
            INVALID_PLAYLIST_TYPE(2),
            INVALID_SONG_TYPE(3),
            INCONSISTENT_DATA(4)
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
     * Updates preset to have a new name.
     */
    suspend fun AppDatabase.renamePreset(preset: DBPreset) = presetDao().updatePreset(preset)

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
                    DBPlaylistType.CLASSIC -> loadClassicPlaylist(it.playlistId, it.name)
                    DBPlaylistType.SPOTIFY -> loadSpotifyPlaylist(it.playlistId, it.name)
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
        DBPlaylistType.CLASSIC -> loadClassicPlaylist(playlistItem)
        DBPlaylistType.SPOTIFY -> loadSpotifyPlaylist(playlistItem)
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
     * Deletes association between playlist and preset with provided ids from database.
     */
    suspend fun AppDatabase.removePlaylistFromPreset(playlistId: Long, presetId: Long) =
        playlistDao().removePlaylistFromPreset(DBPresetPlaylistCrossRef(presetId, playlistId))

    /**
     * Deletes playlist with [playlist]'s [id][DBPlaylist.playlistId] and all its associations with presets from the database.
     * __Remember to call type specific functions to delete the playlist from typed tables!__
     */
    suspend fun AppDatabase.deletePlaylist(playlist: DBPlaylist) {
        playlistDao().deletePlaylist(playlist)
        playlistDao().deletePlaylistFromAllPresets(playlist.playlistId)
        playlistDao().removeAllSongsFromPlaylist(playlist.playlistId)
    }

    /**
     * Deletes all associations between classic playlist with provided [id][playlistId] and songs from database.
     */
    suspend fun AppDatabase.deleteClassicPlaylist(playlistId: Long) = classicPlaylistDao().deletePlaylist(playlistId)

    /**
     * Deletes spotify playlist with [playlist]'s id from database.
     */
    suspend fun AppDatabase.deleteSpotifyPlaylist(playlist: DBSpotifyPlaylist) = spotifyPlaylistsDao().deletePlaylist(playlist)

    /**
     * Loads all songs that aren't already present in playlist with [playlistId] from database.
     * @throws DBException There has been a problem while loading data from database or converting it.
     * See [DBException.code] for more information about the cause.
     */
    suspend fun AppDatabase.loadSongsNotInPlaylist(playlistId: Long): List<Song> {
        val ids = classicPlaylistDao().loadSongIdsFromPlaylist(playlistId)
        val songs = songDao().loadSongsNotInSet(ids)
        return try {
            songs.map {
                when (DBSongType.map(it.type)) {
                    DBSongType.LOCAL -> {
                        val uri = localSongDao().getSongUri(it.songId) ?: throw DBException(DBException.Cause.INCONSISTENT_DATA)
                        LocalSong(it.songId, it.title, uri)
                    }
                }
            }
        } catch (e: TypeCastException) {
            throw DBException(DBException.Cause.INVALID_SONG_TYPE)
        }
    }

    /**
     * Loads uris of all local songs from the database.
     */
    suspend fun AppDatabase.loadLocalSongUris(): List<String> = localSongDao().loadUris()

    /**
     * Loads id of a song with provided [uri] (if any exists).
     */
    suspend fun AppDatabase.loadLocalSongIdByUri(uri: String): Long? = localSongDao().getSongIdByUri(uri)

    /**
     * Adds new song to the database.
     * @return Id of the new [song][DBSong].
     */
    suspend fun AppDatabase.addSong(song: DBSong): Long = songDao().insertSong(song)

    /**
     * Adds new local song to the database.
     */
    suspend fun AppDatabase.addLocalSong(song: DBLocalSong) = localSongDao().insertSong(song)

    /**
     * Adds association between song with [songId] and playlist with [playlistId] to the database.
     */
    suspend fun AppDatabase.addSongToPlaylist(songId: Long, playlistId: Long) =
        songDao().insertSongToPlaylist(DBClassicPlaylistSongCrossRef(playlistId, songId))

    /**
     * Deletes association between song and playlist with provided ids from database.
     */
    suspend fun AppDatabase.removeSongFromPlaylist(songId: Long, playlistId: Long) =
        songDao().removeSongFromPlaylist(DBClassicPlaylistSongCrossRef(playlistId, songId))

    /**
     * Deletes song with [song]'s [id][DBSong.songId] and all its associations with playlists.
     */
    suspend fun AppDatabase.deleteSong(song: DBSong, type: DBSongType) {
        val id = song.songId
        when (type) {
            DBSongType.LOCAL -> localSongDao().deleteSong(DBLocalSong(id, ""))
        }
        songDao().deleteSong(song)
        songDao().deleteSongFromAllPlaylists(id)
    }

    //#region Loading typed playlists
    private suspend fun AppDatabase.loadClassicPlaylist(playlistItem: PlaylistItem): LocalPlaylist? =
        loadClassicPlaylist(playlistItem.id, playlistItem.name)

    private suspend fun AppDatabase.loadClassicPlaylist(id: Long, name: String): LocalPlaylist? {
        val playlist = classicPlaylistDao().loadPlaylist(id) ?: return null
        val typedIdMap = mutableMapOf<DBSongType, ArrayList<DBSong>>()
        DBSongType.values().forEach { typedIdMap[it] = arrayListOf() }
        playlist.songs.forEach {
            try {
                typedIdMap[DBSongType.map(it.type)]?.add(it) ?: throw TypeCastException()
            } catch (e: TypeCastException) {
                throw DBException(DBException.Cause.INVALID_SONG_TYPE)
            }
        }
        val songs: List<Song> = typedIdMap.flatMap { (type, ids) ->
            when (type) {
                DBSongType.LOCAL -> loadLocalSongs(ids)
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
