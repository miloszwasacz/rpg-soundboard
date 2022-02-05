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
    abstract var name: String
    abstract val type: SongType
}

@Serializable
@SerialName("song.local")
class LocalSong(override var name: String, private val uri: String): Song() {
    @Transient
    override val type: SongType = SongType.LOCAL

    fun getUri() = Uri.parse(uri)
}