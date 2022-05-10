package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPlaylistBinding
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBPlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem

class PlaylistAdapter(list: List<PlaylistItem>, private val navController: NavController) :
    DataBindingListAdapter<ListItemPlaylistBinding, PlaylistItem>(list, ListItemPlaylistBinding::inflate) {
    override fun onBindViewHolder(holder: ViewHolder<ListItemPlaylistBinding>, position: Int) {
        holder.binding.apply {
            val playlistItem = list[position]
            cardView.setOnClickListener {
                when (playlistItem.type) {
                    DBPlaylistType.PlaylistType.CLASSIC -> {
                        val action = PlaylistFragmentDirections.navigationLibraryPlaylistsToSongs(playlistItem, playlistItem.name)
                        val extras = FragmentNavigatorExtras(
                            cardView to root.resources.getString(R.string.transition_name_playlist, playlistItem.id)
                        )
                        navController.navigate(action, extras)
                    }
                    DBPlaylistType.PlaylistType.SPOTIFY -> {
                        //TODO Open playlist in spotify
                    }
                }
            }
            updateBindings {
                playlist = playlistItem
            }
        }
    }
}