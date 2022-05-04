package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import android.net.Uri
import com.gmail.dev.wasacz.rpgsoundboard.model.SongType

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