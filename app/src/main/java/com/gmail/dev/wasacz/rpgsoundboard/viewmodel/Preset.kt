package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import com.gmail.dev.wasacz.rpgsoundboard.model.Preset as ModelPreset

class Preset(private val preset: ModelPreset) {
    val name by preset::name
    val playlists: PlaylistList = PlaylistList(preset.playlists)
}