package com.gmail.dev.wasacz.rpgsoundboard.viewmodel

import com.gmail.dev.wasacz.rpgsoundboard.model.SongType
import com.gmail.dev.wasacz.rpgsoundboard.model.LocalSong as ModelLocalSong
import com.gmail.dev.wasacz.rpgsoundboard.model.Song as ModelSong

class SongList(private val list: List<ModelSong>) : List<Song> {
    override fun get(index: Int): LocalSong = when (list[index].type) {
        SongType.LOCAL -> LocalSong(list[index] as ModelLocalSong)
    }

    override val size: Int by list::size

    override fun contains(element: Song): Boolean = list.contains(element.song)

    override fun containsAll(elements: Collection<Song>): Boolean = list.containsAll(elements.map { it.song })

    override fun indexOf(element: Song): Int = list.indexOf(element.song)

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): Iterator<Song> = list.map {
        when (it.type) {
            SongType.LOCAL -> LocalSong(it as ModelLocalSong)
        }
    }.iterator()

    override fun lastIndexOf(element: Song): Int = list.lastIndexOf(element.song)

    override fun listIterator(): ListIterator<Song> = list.map {
        when (it.type) {
            SongType.LOCAL -> LocalSong(it as ModelLocalSong)
        }
    }.listIterator()

    override fun listIterator(index: Int): ListIterator<Song> = list.map {
        when (it.type) {
            SongType.LOCAL -> LocalSong(it as ModelLocalSong)
        }
    }.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<Song> = list.map {
        when (it.type) {
            SongType.LOCAL -> LocalSong(it as ModelLocalSong)
        }
    }.subList(fromIndex, toIndex)
}