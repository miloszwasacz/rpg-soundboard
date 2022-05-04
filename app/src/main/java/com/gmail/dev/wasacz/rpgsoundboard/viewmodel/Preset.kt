package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

/*class Preset(private val preset: ModelPreset) {
    val name by preset::name
}*/

open class Preset(
    val id: Long,
    val name: String
)

class PresetFull(preset: Preset, val playlists: ArrayList<Playlist>) : Preset(preset.id, preset.name)