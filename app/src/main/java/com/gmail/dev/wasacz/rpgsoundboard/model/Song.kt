package com.gmail.dev.wasacz.rpgsoundboard.model

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class SongType {
    LOCAL
}

@Serializable
sealed class Song {
    abstract val id: Int
    abstract var title: String
    abstract val type: SongType
}

@Serializable
@SerialName("song.local")
class LocalSong(override val id: Int, override var title: String, private val _uri: String) : Song() {
    val uri: Uri by lazy { Uri.parse(_uri) }

    @Transient
    override val type: SongType = SongType.LOCAL
}
