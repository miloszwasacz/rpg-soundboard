package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class AlertDialogFragment(
    @StringRes private val title: Int,
    @StringRes private val confirmButtonText: Int,
    @StringRes private val message: Int?,
    @DrawableRes private val icon: Int?
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =
            if (icon == null) MaterialAlertDialogBuilder(requireContext())
            else MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Centered)
        return builder.apply {
            setTitle(title)
            message?.let { setMessage(it) }
            icon?.let { setIcon(it) }
            setPositiveButton(confirmButtonText) { _, _ ->
                onConfirm()
            }
            setNegativeButton(R.string.action_cancel) { _, _ ->
                onCancel()
            }
        }.create()
    }

    abstract fun onConfirm()

    @CallSuper
    open fun onCancel() {
        findNavController().navigateUp()
    }
}