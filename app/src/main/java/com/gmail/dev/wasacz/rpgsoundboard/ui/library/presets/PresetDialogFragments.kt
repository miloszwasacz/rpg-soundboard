package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.DialogAddPresetBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.AlertDialogFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.setNavigationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddPresetFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbViewModel) }
        val binding = DialogAddPresetBinding.inflate(layoutInflater)
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_add_preset)
            .setView(binding.root)
            .setPositiveButton(R.string.action_add) { _, _ ->
                binding.inputLayout.editText?.text?.toString()?.let {
                    val name = it.trim()
                    if (name.isNotBlank()) {
                        lifecycleScope.launchWhenResumed {
                            val (id, presetName) = viewModel.addPreset(name)
                            requireDialog().dismiss()
                            val action = AddPresetFragmentDirections.navigationNewPresetToPlaylists(id, presetName)
                            findNavController().navigate(action)
                        }
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel) { _, _ ->
                findNavController().navigateUp()
            }
            .create().also {
                it.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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