package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

import android.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPlaylistBinding
import com.gmail.dev.wasacz.rpgsoundboard.model.db.DBPlaylistType
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.IContextMenuAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.ui.navigate
import com.gmail.dev.wasacz.rpgsoundboard.ui.toggle
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale

class PlaylistAdapter(
    list: List<PlaylistItem>,
    private val fragment: Fragment,
    override val startActionMode: () -> ActionMode?
) : DataBindingListAdapter<ListItemPlaylistBinding, PlaylistItem>(list, ListItemPlaylistBinding::inflate), IContextMenuAdapter {
    override var actionMode: ActionMode? = null
    private val selected = mutableSetOf<Int>()

    override fun onBindViewHolder(holder: ViewHolder<ListItemPlaylistBinding>, position: Int) {
        val resources = holder.itemView.resources
        holder.binding.apply {
            val playlistItem = list[position]
            cardView.setOnClickListener {
                if (actionMode == null) {
                    when (playlistItem.type) {
                        DBPlaylistType.CLASSIC -> {
                            val action = PresetFragmentDirections.navigationLibraryToPlaylist(playlistItem, playlistItem.name)
                            val extras = FragmentNavigatorExtras(
                                cardView to resources.getString(R.string.transition_name_playlist, playlistItem.id)
                            )
                            fragment.navigate(action, extras) {
                                sharedEnter = MaterialContainerTransform().apply { drawingViewId = R.id.nav_host_fragment }
                                exit = MaterialElevationScale(false)
                                reenter = MaterialElevationScale(true)
                            }
                        }
                        DBPlaylistType.SPOTIFY -> {
                            //TODO Open playlist in spotify
                        }
                    }
                } else cardView.performLongClick()
            }
            cardView.setOnLongClickListener {
                selectItem(position)
                if (actionMode == null) actionMode = startActionMode()
                actionMode!!.let {
                    it.title = resources.getString(R.string.action_title_items_selected, selected.size)
                    if (selected.size == 0) finishActionMode()
                }
                true
            }
            cardView.isChecked = selected.contains(position)
            updateBindings {
                playlist = playlistItem
            }
        }
    }

    private fun selectItem(position: Int) {
        selected.toggle(position)
        notifyItemChanged(position)
    }

    fun getSelectedItems(): List<PlaylistItem> = selected.map { list[it] }

    override fun onCreateActionMode() {}

    override fun onDestroyActionMode() {
        actionMode = null
        val indices = selected.toList()
        selected.clear()
        for (i in indices)
            notifyItemChanged(i)
    }
}