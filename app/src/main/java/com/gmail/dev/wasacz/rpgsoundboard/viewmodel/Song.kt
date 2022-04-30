package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import io.github.esentsov.PackagePrivate
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong as ModelLocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.Song as ModelSong

sealed class Song(@PackagePrivate val song: ModelSong) {
    val title by song::title
    val type by song::type
}

class LocalSong(modelSong: ModelLocalSong): Song(modelSong) {
    val uri by modelSong::uri
}