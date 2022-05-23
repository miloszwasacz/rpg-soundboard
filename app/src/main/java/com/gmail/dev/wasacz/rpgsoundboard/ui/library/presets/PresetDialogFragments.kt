package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.AlertDialogFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SingleInputDialogFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.setNavigationResult
import kotlinx.coroutines.launch

class CreatePresetFragment : SingleInputDialogFragment(
    R.string.dialog_title_new_preset,
    R.string.hint_name,
    R.string.action_create
) {
    private lateinit var viewModel: PresetViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbViewModel) }
        this.viewModel = viewModel
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onConfirm() {
        getInputText()?.let {
            val name = it.trim()
            if (name.isNotBlank()) {
                lifecycleScope.launch {
                    val (id, presetName) = viewModel.addPreset(name)
                    val action = CreatePresetFragmentDirections.navigationNewPresetToPlaylists(id, presetName)
                    findNavController().navigate(action)
                    requireDialog().dismiss()
                }
            }
        }
    }
}

class DeletePresetsFragment : AlertDialogFragment(
    title = R.string.dialog_title_delete_presets,
    message = R.string.dialog_message_delete_presets,
    icon = R.drawable.ic_delete_24dp,
    confirmButtonText = R.string.action_delete
) {
    private lateinit var viewModel: PresetViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbViewModel) }
        this.viewModel = viewModel
        return dialog
    }

    override fun onConfirm() {
        setNavigationResult(R.string.nav_arg_delete_presets_result, true)
        findNavController().navigateUp()
    }

    override fun onCancel() {
        setNavigationResult(R.string.nav_arg_delete_presets_result, false)
        super.onCancel()
    }

    override fun onCancel(dialog: DialogInterface) {
        setNavigationResult(R.string.nav_arg_delete_presets_result, false)
        super.onCancel(dialog)
    }
}