package com.gmail.dev.wasacz.rpgsoundboard.model.db

import androidx.room.*
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType

object DBSongType {
    const val LOCAL = "local"


    /**
     * Converts enum type to database-specific type.
     */
    fun map(type: SongType): String = when(type) {
        SongType.LOCAL -> LOCAL
    }

    /**
     * Converts database-specific type string to enum type.
     * @throws TypeCastException No corresponding enum type exists for provided string
     */
    fun map(type: String): SongType = when (type) {
        LOCAL -> SongType.LOCAL
        else -> throw TypeCastException()
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
    abstract suspend fun insertSongs(vararg songs: T): List<Long>

    @Update
    abstract suspend fun updateSong(songs: T)

    @Delete
    abstract suspend fun deleteSong(songs: T)

    /*@Query(
        value = "SELECT * FROM songs " +
                "WHERE songId IN (:songIds)"
    )
    abstract suspend fun loadSongs(songIds: List<Int>): List<T>*/
}

@Dao
abstract class SongDao : TypedSongDao<DBSong>() {
    @Query("SELECT * FROM songs")
    abstract suspend fun loadSongs(): List<DBSong>

    @Query(
        value = "SELECT * FROM songs " +
                "WHERE songId IN (:songIds)"
    )
    abstract suspend fun loadSongs(songIds: List<Long>): List<DBSong>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSongToPlaylist(songWithPlaylist: DBClassicPlaylistSongCrossRef)
}

@Dao
abstract class LocalSongDao : TypedSongDao<DBLocalSong>() {
    /*@Query(
        value = "SELECT * FROM songs " +
                "INNER JOIN local_songs ON local_songs.songId = songId " +
                "WHERE type = ${DBSongType.LOCAL}"
    )
    abstract suspend fun loadSongs(): List<DBLocalSong>*/

    @Query(
        value = "SELECT * FROM local_songs " +
                "WHERE songId IN (:songIds)"
    )
    abstract suspend fun loadSongs(songIds: List<Long>): List<DBLocalSong>
}