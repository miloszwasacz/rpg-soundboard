package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import android.widget.Toast
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPlaylistBinding
import com.gmail.dev.wasacz.rpgsoundboard.model.PlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.LocalPlaylist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.SpotifyPlaylist

class LibraryAdapter(list: List<Playlist>) : DataBindingListAdapter<ListItemPlaylistBinding, Playlist>(list, ListItemPlaylistBinding::inflate) {
    override fun onBindViewHolder(holder: ViewHolder<ListItemPlaylistBinding>, position: Int) {
        holder.itemView.setOnClickListener {
            val item = list[position]
            Toast.makeText(
                holder.itemView.context,
                if (item.type == PlaylistType.SPOTIFY) (item as SpotifyPlaylist).uri
                else (item as LocalPlaylist).songList.joinToString("\n") { "${it.title}: ${it.id}" },
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.binding.updateBindings {
            title = list[position].name
        }
    }
}