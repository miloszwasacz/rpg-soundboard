package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import com.gmail.dev.wasacz.rpgsoundboard.model.PlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalPlaylist as ModelLocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.model.Playlist as ModelPlaylist

class PlaylistList(private val list: List<ModelPlaylist>) : List<Playlist> {
    override fun get(index: Int): Playlist = when (list[index].type) {
        PlaylistType.LOCAL -> LocalPlaylist(list[index] as ModelLocalPlaylist)
    }

    override val size: Int by list::size

    override fun contains(element: Playlist): Boolean = list.contains(element.playlist)

    override fun containsAll(elements: Collection<Playlist>): Boolean = list.containsAll(elements.map { it.playlist })

    override fun indexOf(element: Playlist): Int = list.indexOf(element.playlist)

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): Iterator<Playlist> = list.map {
        when (it.type) {
            PlaylistType.LOCAL -> LocalPlaylist(it as ModelLocalPlaylist)
        }
    }.iterator()

    override fun lastIndexOf(element: Playlist): Int = list.lastIndexOf(element.playlist)

    override fun listIterator(): ListIterator<Playlist> = list.map {
        when (it.type) {
            PlaylistType.LOCAL -> LocalPlaylist(it as ModelLocalPlaylist)
        }
    }.listIterator()

    override fun listIterator(index: Int): ListIterator<Playlist> = list.map {
        when (it.type) {
            PlaylistType.LOCAL -> LocalPlaylist(it as ModelLocalPlaylist)
        }
    }.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<Playlist> = list.map {
        when (it.type) {
            PlaylistType.LOCAL -> LocalPlaylist(it as ModelLocalPlaylist)
        }
    }.subList(fromIndex, toIndex)
}