package com.gmail.dev.wasacz.rpgsoundboard.model.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DBPreset::class,
        DBPresetPlaylistCrossRef::class,
        DBPlaylist::class,
        DBSpotifyPlaylist::class,
        DBClassicPlaylistSongCrossRef::class,
        DBSong::class,
        DBLocalSong::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    //#region Presets
    abstract fun presetDao(): PresetDao
    //#endregion
    //#region Playlists
    abstract fun playlistDao() : PlaylistDao
    abstract fun classicPlaylistDao(): ClassicPlaylistDao
    abstract fun spotifyPlaylistsDao(): SpotifyPlaylistDao
    //#endregion
    //#region Songs
    abstract fun songDao(): SongDao
    abstract fun localSongDao(): LocalSongDao
    //#endregion
}