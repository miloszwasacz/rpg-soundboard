package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBLocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBSong
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBSongType

/*sealed class Song(@PackagePrivate val song: ModelSong) {
    val title by song::title
    val type by song::type
}

class LocalSong(modelSong: ModelLocalSong): Song(modelSong) {
    val uri by modelSong::uri
}*/

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
    val uri: Uri by lazy { ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id) }

    fun toDBSong(): DBSong = DBSong(title = name, type = DBSongType.LOCAL.name)
    fun toDBLocalSong(id: Long): DBLocalSong = DBLocalSong(id, uri.toString())
}