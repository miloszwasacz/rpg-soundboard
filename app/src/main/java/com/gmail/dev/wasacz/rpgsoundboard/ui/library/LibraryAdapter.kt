package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import android.widget.Toast
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPlaylistBinding
import com.gmail.dev.wasacz.rpgsoundboard.model.Item
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings

class LibraryAdapter(list: List<Item>) : DataBindingListAdapter<ListItemPlaylistBinding, Item>(list, ListItemPlaylistBinding::inflate) {
    override fun onBindViewHolder(holder: ViewHolder<ListItemPlaylistBinding>, position: Int) {
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, list[position].title, Toast.LENGTH_SHORT).show()
        }
        holder.binding.updateBindings {
            title = list[position].title
        }
    }
}