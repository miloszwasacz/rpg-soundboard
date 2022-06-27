package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBLocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBSong
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBSongType

enum class SongType(val dbType: DBSongType) {
    LOCAL(DBSongType.LOCAL)
}

sealed class Song {
    abstract val id: Long
    abstract var title: String
    abstract val type: SongType
}

class LocalSong(override val id: Long, override var title: String, private val _uri: String) : Song() {
    val uri: Uri by lazy { Uri.parse(_uri) }
    override val type: SongType = SongType.LOCAL
}

class TempLocalSong(private val id: Long, val name: String) {
    private var _uri: Uri? = null
    val uri: Uri by lazy { _uri ?: ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id) }
    val localStorageId get() = id

    constructor(name: String, uri: Uri) : this(-1, name) {
        _uri = uri
    }

    fun toDBSong(): DBSong = DBSong(title = name, type = DBSongType.LOCAL.name)
    fun toDBLocalSong(id: Long): DBLocalSong = DBLocalSong(id, uri.toString())

    companion object {
        fun getIdFromUri(uri: String): Long? {
            val regex = "^${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/(\\d+)$".toRegex()
            return regex.find(uri)?.groupValues?.takeIf { it.size >= 2 }?.get(1)?.toLongOrNull()
        }
    }
}