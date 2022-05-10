package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemSongBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song

class SongAdapter(list: List<Song>) : DataBindingListAdapter<ListItemSongBinding, Song>(list, ListItemSongBinding::inflate) {
    override fun onBindViewHolder(holder: ViewHolder<ListItemSongBinding>, position: Int) {
        holder.binding.apply {
            updateBindings {
                song = list[position]
            }
        }
    }
}