package com.gmail.dev.wasacz.rpgsoundboard.model.db

import androidx.room.*

@Entity(tableName = "presets")
data class DBPreset(
    @PrimaryKey(autoGenerate = true) val presetId: Long = 0,
    val name: String
)

@Entity(
    tableName = "preset_cross_ref",
    primaryKeys = ["presetId", "playlistId"]
)
data class DBPresetPlaylistCrossRef(
    @ColumnInfo(index = true) val presetId: Long,
    @ColumnInfo(index = true) val playlistId: Long
)

data class DBPresetWithPlaylists(
    @Embedded val preset: DBPreset,
    @Relation(
        parentColumn = "presetId",
        entityColumn = "playlistId",
        associateBy = Junction(DBPresetPlaylistCrossRef::class)
    )
    val playlists: List<DBPlaylist>
)

@Dao
interface PresetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: DBPreset): Long

    @Update
    suspend fun updatePreset(preset: DBPreset)

    @Delete
    suspend fun deletePreset(preset: DBPreset)

    @Query("DELETE FROM preset_cross_ref WHERE presetId = :presetId")
    suspend fun deletePresetPlaylists(presetId: Long)

    @Query("SELECT * FROM presets")
    suspend fun loadPresets(): List<DBPreset>

    @Transaction
    @Query(
        value = "SELECT * FROM presets " +
                "WHERE presetId = :presetId"
    )
    suspend fun loadPlaylistsFromPreset(presetId: Long): List<DBPresetWithPlaylists>

    @Query(
        value = "SELECT playlistId FROM preset_cross_ref " +
                "WHERE presetId = :presetId"
    )
    suspend fun loadPlaylistIdsFromPreset(presetId: Long): List<Long>
}