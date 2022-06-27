package com.gmail.dev.wasacz.rpgsoundboard.model.db

import androidx.room.*
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType

enum class DBSongType {
    LOCAL;

    companion object {
        /**
         * Converts enum type to database-specific type.
         */
        fun map(type: SongType): DBSongType = type.dbType

        /**
         * Converts database-specific type string to enum type.
         * @throws TypeCastException No corresponding enum type exists for provided string.
         */
        fun map(type: String): DBSongType = try {
            enumValueOf(type)
        } catch (e: IllegalArgumentException) {
            throw TypeCastException()
        }
    }
}

@Entity(tableName = "songs")
data class DBSong(
    @PrimaryKey(autoGenerate = true) val songId: Long = 0,
    val title: String,
    val type: String
)

@Entity(tableName = "local_songs")
data class DBLocalSong(
    @PrimaryKey val songId: Long,
    val uri: String
)

abstract class TypedSongDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSong(song: T): Long

    @Update
    abstract suspend fun updateSong(songs: T)

    @Delete
    abstract suspend fun deleteSong(songs: T)
}

@Dao
abstract class SongDao : TypedSongDao<DBSong>() {
    @Transaction
    @Query("SELECT * FROM songs")
    abstract suspend fun loadSongs(): List<DBSong>

    @Transaction
    @Query(
        value = "SELECT * FROM songs " +
                "WHERE songId IN (:songIds)"
    )
    abstract suspend fun loadSongs(songIds: List<Long>): List<DBSong>

    @Transaction
    @Query(
        value = "SELECT * FROM songs " +
                "WHERE songId NOT IN (:songIds)"
    )
    abstract suspend fun loadSongsNotInSet(songIds: List<Long>): List<DBSong>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSongToPlaylist(songWithPlaylist: DBClassicPlaylistSongCrossRef)

    @Delete
    abstract suspend fun removeSongFromPlaylist(songWithPlaylist: DBClassicPlaylistSongCrossRef)

    @Transaction
    @Query("DELETE FROM classic_playlist_cross_ref WHERE songId = :songId")
    abstract suspend fun deleteSongFromAllPlaylists(songId: Long)
}

@Dao
abstract class LocalSongDao : TypedSongDao<DBLocalSong>() {
    @Transaction
    @Query(
        value = "SELECT * FROM local_songs " +
                "WHERE songId IN (:songIds)"
    )
    abstract suspend fun loadSongs(songIds: List<Long>): List<DBLocalSong>

    @Transaction
    @Query("SELECT uri FROM local_songs")
    abstract suspend fun loadUris(): List<String>

    @Transaction
    @Query(
        value = "SELECT uri FROM local_songs " +
                "WHERE songId = :songId"
    )
    abstract suspend fun getSongUri(songId: Long): String?

    @Transaction
    @Query(
        value = "SELECT songId FROM local_songs " +
                "WHERE uri = :uri"
    )
    abstract suspend fun getSongIdByUri(uri: String): Long?
}