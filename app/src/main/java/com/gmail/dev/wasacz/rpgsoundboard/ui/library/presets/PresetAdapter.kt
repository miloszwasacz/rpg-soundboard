package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.view.ActionMode
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemSelectableBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.IContextMenuAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectableItemListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.ui.navigate
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import com.google.android.material.appbar.MaterialToolbar

class PresetAdapter(
    list: List<Preset>,
    private val navController: NavController,
    private val toolbar: MaterialToolbar,
    override val startActionMode: () -> ActionMode?,
    private val onItemClickAnimationSetter: () -> Unit
) : SelectableItemListAdapter<Preset>(list, { it.name }), IContextMenuAdapter {
    override var actionMode: ActionMode? = null

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
                    if (selected.size == 0) finishActionMode()
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
                finishActionMode()
                val action = LibraryFragmentDirections.navigationLibraryToPreset(item.id, item.name)
                val extras = FragmentNavigatorExtras(
                    itemLayout to root.resources.getString(R.string.transition_name_preset, item.id)
                )
                navController.navigate(action, toolbar, extras)
            } else itemLayout.performLongClick()
        }
        return true
    }

    override fun onCreateActionMode() {
        notifyItemRangeChanged(0, list.size)
    }

    override fun onDestroyActionMode() {
        actionMode = null
        notifyItemRangeChanged(0, list.size)
        selected.clear()
    }
}