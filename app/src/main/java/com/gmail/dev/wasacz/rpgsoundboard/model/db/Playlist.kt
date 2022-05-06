package com.gmail.dev.wasacz.rpgsoundboard.model.db

import androidx.room.*
import com.gmail.dev.wasacz.rpgsoundboard.model.PlaylistType as ModelPlaylistType

object DBPlaylistType {
    enum class PlaylistType(val value: String) {
        CLASSIC (DBPlaylistType.CLASSIC),
        SPOTIFY (DBPlaylistType.SPOTIFY)
    }
    const val CLASSIC = "classic"
    const val SPOTIFY = "spotify"

    /**
     * Converts enum type to database-specific type.
     */
    fun map(type: ModelPlaylistType): PlaylistType = when (type) {
        ModelPlaylistType.LOCAL -> PlaylistType.CLASSIC
        ModelPlaylistType.SPOTIFY -> PlaylistType.SPOTIFY
    }

    /**
     * Converts database-specific type string to enum type.
     * @throws TypeCastException No corresponding enum type exists for provided string
     */
    fun map(type: String): PlaylistType = when (type) {
        CLASSIC -> PlaylistType.CLASSIC
        SPOTIFY -> PlaylistType.SPOTIFY
        else -> throw TypeCastException()
    }
}

@Entity(tableName = "playlists")
data class DBPlaylist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val type: String
)

@Entity(tableName = "classic_playlists")
data class DBClassicPlaylist(
    @PrimaryKey val playlistId: Long
)

@Entity(
    tableName = "classic_playlist_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")]
)
data class DBClassicPlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)

data class DBClassicPlaylistWithSongs(
    @Embedded val playlist: DBClassicPlaylist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "songId",
        associateBy = Junction(DBClassicPlaylistSongCrossRef::class)
    )
    val songs: List<DBSong>
)

@Entity(tableName = "spotify_playlists")
data class DBSpotifyPlaylist(
    @PrimaryKey val playlistId: Long,
    val uri: String
)

abstract class GenericPlaylistDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylist(playlist: T): Long

    @Update
    abstract suspend fun updatePlaylist(playlist: T)

    @Delete
    abstract suspend fun deletePlaylist(playlist: T)
}

abstract class TypedPlaylistDao<T, U>(private val type: String) : GenericPlaylistDao<T>() {
    @Query(
        value = "SELECT * FROM playlists " +
                "WHERE type = :type"
    )
    abstract suspend fun loadPlaylists(type: String = this.type): List<DBPlaylist>

    abstract suspend fun loadPlaylist(id: Long): U?
}

@Dao
abstract class PlaylistDao : GenericPlaylistDao<DBPlaylist>() {
    @Query("SELECT * FROM playlists")
    abstract suspend fun loadPlaylists(): List<DBPlaylist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylistToPreset(playlistWithPreset: DBPresetPlaylistCrossRef)
}

@Dao
abstract class ClassicPlaylistDao : TypedPlaylistDao<DBClassicPlaylist, DBClassicPlaylistWithSongs>(DBPlaylistType.CLASSIC) {
    /*@Query(
        value = "SELECT * FROM playlists " +
                "WHERE type = ${DBPlaylistType.CLASSIC}"
    )
    abstract suspend fun loadPlaylists(): List<DBClassicPlaylistWithSongs>*/
    @Transaction
    @Query(
        value = "SELECT songId FROM classic_playlist_cross_ref " +
                "WHERE playlistId = :id"
    )
    abstract suspend fun loadSongIdsFromPlaylist(id: Long): List<Long>

    @Transaction
    @Query(
        value = "SELECT * FROM classic_playlists " +
                "WHERE playlistId = :id"
    )
    abstract override suspend fun loadPlaylist(id: Long): DBClassicPlaylistWithSongs?
}

@Dao
abstract class SpotifyPlaylistDao : TypedPlaylistDao<DBSpotifyPlaylist, DBSpotifyPlaylist>(DBPlaylistType.SPOTIFY) {
    /*@Query(
        value = "SELECT * FROM playlists " +
                "WHERE type = ${DBPlaylistType.SPOTIFY}"
    )
    abstract suspend fun loadPlaylists(): List<DBSpotifyPlaylist>*/

    @Query(
        value = "SELECT * FROM spotify_playlists " +
                "WHERE playlistId = :id"
    )
    abstract override suspend fun loadPlaylist(id: Long): DBSpotifyPlaylist?
}