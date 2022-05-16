package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.view.ActionMode
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemSelectableBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectableItemListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset

class PresetAdapter(
    list: List<Preset>,
    private val navController: NavController,
    private val startActionMode: () -> ActionMode?,
    private val onItemClickAnimationSetter: () -> Unit
) : SelectableItemListAdapter<Preset>(list, { it.name }) {
    private var actionMode: ActionMode? = null
    private var size = list.size

    override fun getItemCount(): Int = size

    override fun onBindViewHolder(holder: ViewHolder<ListItemSelectableBinding>, position: Int) {
        val resources = holder.itemView.resources
        holder.binding.apply {
            itemLayout.transitionName = resources.getString(R.string.transition_name_preset, list[position].id)
            itemLayout.setOnLongClickListener {
                updateBindings {
                    isSelectable = true
                }
                selectItem(this, position)
                if (actionMode == null) actionMode = startActionMode()
                actionMode!!.let {
                    it.title = resources.getString(R.string.action_title_items_selected, selected.size)
                    if (selected.size == 0) it.finish()
                }
                true
            }
        }
        super.onBindViewHolder(holder, position)
        holder.binding.updateBindings {
            isSelectable = actionMode != null
        }
    }

    override fun onItemClick(item: Preset, binding: ListItemSelectableBinding): Boolean {
        with(binding) {
            if (actionMode == null) {
                onItemClickAnimationSetter()
                actionMode?.finish()
                val action = PresetFragmentDirections.navigationLibraryPresetsToPlaylists(item.id, item.name)
                val extras = FragmentNavigatorExtras(
                    itemLayout to root.resources.getString(R.string.transition_name_preset, item.id)
                )
                navController.navigate(action, extras)
            } else itemLayout.performLongClick()
        }
        return true
    }

    fun notifyItemsRemoved() {
        for (i in selected) {
            size--
            notifyItemRemoved(i)
        }
    }

    fun finishActionMode() {
        actionMode?.finish()
    }

    fun onCreateActionMode() {
        notifyItemRangeChanged(0, size)
    }

    fun onDestroyActionMode() {
        actionMode = null
        notifyItemRangeChanged(0, size)
        selected.clear()
    }
}