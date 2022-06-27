package com.gmail.dev.wasacz.rpgsoundboard.model.db

import androidx.room.*
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistType as ModelPlaylistType

enum class DBPlaylistType {
    CLASSIC,
    SPOTIFY;

    companion object {
        /**
         * Converts enum type to database-specific type.
         */
        fun map(type: ModelPlaylistType): DBPlaylistType = type.dbType

        /**
         * Converts database-specific type string to enum type.
         * @throws TypeCastException No corresponding enum type exists for provided string
         */
        fun map(type: String): DBPlaylistType = try {
            enumValueOf(type)
        } catch (e: IllegalArgumentException) {
            throw TypeCastException()
        }
    }
}

@Entity(tableName = "playlists")
data class DBPlaylist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val type: String
)

@Entity(
    tableName = "classic_playlist_cross_ref",
    primaryKeys = ["playlistId", "songId"]
)
data class DBClassicPlaylistSongCrossRef(
    @ColumnInfo(index = true) val playlistId: Long,
    @ColumnInfo(index = true) val songId: Long
)

data class DBClassicPlaylistWithSongs(
    @Embedded val playlist: DBPlaylist,
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
    @Query(
        value = "SELECT * FROM playlists " +
                "WHERE playlistId NOT IN (:ids)"
    )
    abstract suspend fun loadPlaylistsNotInSet(ids: List<Long>): List<DBPlaylist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPlaylistToPreset(playlistWithPreset: DBPresetPlaylistCrossRef)

    @Delete
    abstract suspend fun removePlaylistFromPreset(playlistWithPreset: DBPresetPlaylistCrossRef)

    @Transaction
    @Query("DELETE FROM preset_cross_ref WHERE playlistId = :playlistId")
    abstract suspend fun deletePlaylistFromAllPresets(playlistId: Long)

    @Transaction
    @Query("DELETE FROM classic_playlist_cross_ref WHERE playlistId = :playlistId")
    abstract suspend fun removeAllSongsFromPlaylist(playlistId: Long)
}

@Dao
abstract class ClassicPlaylistDao : TypedPlaylistDao<DBPlaylist, DBClassicPlaylistWithSongs>(DBPlaylistType.CLASSIC.name) {
    @Transaction
    @Query(
        value = "SELECT songId FROM classic_playlist_cross_ref " +
                "WHERE playlistId = :id"
    )
    abstract suspend fun loadSongIdsFromPlaylist(id: Long): List<Long>

    @Transaction
    @Query(
        value = "SELECT * FROM playlists " +
                "WHERE playlistId = :id"
    )
    abstract override suspend fun loadPlaylist(id: Long): DBClassicPlaylistWithSongs?

    @Transaction
    @Query("DELETE FROM classic_playlist_cross_ref WHERE playlistId = :playlistId")
    abstract suspend fun deletePlaylist(playlistId: Long)
}

@Dao
abstract class SpotifyPlaylistDao : TypedPlaylistDao<DBSpotifyPlaylist, DBSpotifyPlaylist>(DBPlaylistType.SPOTIFY.name) {
    @Transaction
    @Query(
        value = "SELECT * FROM spotify_playlists " +
                "WHERE playlistId = :id"
    )
    abstract override suspend fun loadPlaylist(id: Long): DBSpotifyPlaylist?
}