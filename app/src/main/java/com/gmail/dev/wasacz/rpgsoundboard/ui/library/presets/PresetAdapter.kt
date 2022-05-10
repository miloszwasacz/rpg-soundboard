package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPresetBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.updateBindings
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset

class PresetAdapter(list: List<Preset>, private val navController: NavController, private val onItemClickAnimationSetter: () -> Unit) :
    DataBindingListAdapter<ListItemPresetBinding, Preset>(list, ListItemPresetBinding::inflate) {
    override fun onBindViewHolder(holder: ViewHolder<ListItemPresetBinding>, position: Int) {
        holder.binding.apply {
            val preset = list[position]
            cardView.setOnClickListener {
                onItemClickAnimationSetter()
                val action = PresetFragmentDirections.navigationLibraryPresetsToPlaylists(preset.id, preset.name)
                val extras = FragmentNavigatorExtras(
                    cardView to root.resources.getString(R.string.transition_name_preset, preset.id)
                )
                navController.navigate(action, extras)
            }
            updateBindings {
                this.preset = preset
            }
        }
    }
}