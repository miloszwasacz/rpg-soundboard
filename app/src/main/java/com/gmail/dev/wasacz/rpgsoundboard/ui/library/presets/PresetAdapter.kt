package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemSelectableBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.IContextMenuAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectableItemListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.ui.navigate
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale

class PresetAdapter(
    list: List<Preset>,
    private val fragment: Fragment,
    override val startActionMode: () -> ActionMode?
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
                finishActionMode()
                val action = LibraryFragmentDirections.navigationLibraryToPreset(item.id, item.name)
                val extras = FragmentNavigatorExtras(
                    itemLayout to root.resources.getString(R.string.transition_name_preset, item.id)
                )
                fragment.navigate(action, extras) {
                    sharedEnter = MaterialContainerTransform().apply { drawingViewId = R.id.nav_host_fragment }
                    exit = MaterialElevationScale(false)
                    reenter = MaterialElevationScale(true)
                }
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