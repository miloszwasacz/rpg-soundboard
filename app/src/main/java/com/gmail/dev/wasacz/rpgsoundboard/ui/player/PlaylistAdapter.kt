package com.gmail.dev.wasacz.rpgsoundboard.ui.player

import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPlaylistBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist

class PlaylistAdapter(list: List<Playlist>, private val playerStart: (playlist: Playlist) -> Unit) :
    DataBindingListAdapter<ListItemPlaylistBinding, Playlist>(list, ListItemPlaylistBinding::inflate) {
    override fun onBindViewHolder(holder: ViewHolder<ListItemPlaylistBinding>, position: Int) {
        holder.itemView.setOnClickListener {
            playerStart(list[position])
        }
        holder.binding.updateBindings {

        }
    }
}