package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.DialogSingleInputBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

interface IGenericDialogFragment {
    fun onConfirm()
    fun onCancel()
}

abstract class AlertDialogFragment(
    @StringRes private val title: Int,
    @StringRes private val confirmButtonText: Int,
    @StringRes private val cancelButtonText: Int = R.string.action_cancel,
    @StringRes private val message: Int? = null,
    @DrawableRes private val icon: Int? = null
) : DialogFragment(), IGenericDialogFragment {
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
            setNegativeButton(cancelButtonText) { _, _ ->
                onCancel()
            }
        }.create()
    }

    @CallSuper
    override fun onCancel() {
        findNavController().navigateUp()
    }
}

abstract class SingleInputDialogFragment(
    @StringRes private val title: Int,
    @StringRes private val inputHint: Int,
    @StringRes private val confirmButtonText: Int,
    @StringRes private val cancelButtonText: Int = R.string.action_cancel,
    @DrawableRes private val icon: Int? = null
) : DialogFragment(), IGenericDialogFragment {
    private var binding: DialogSingleInputBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSingleInputBinding.inflate(layoutInflater).apply {
            hint = resources.getString(inputHint)
        }
        setInitText()?.let {
            binding?.inputLayout?.editText?.apply {
                setText(it)
                setSelection(length())
            }
        }
        val builder =
            if (icon == null) MaterialAlertDialogBuilder(requireContext())
            else MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Centered)
        return builder.apply {
            setTitle(title)
            icon?.let { setIcon(it) }
            binding?.let { setView(it.root) }
            setPositiveButton(confirmButtonText) { _, _ ->
                onConfirm()
            }
            setNegativeButton(cancelButtonText) { _, _ ->
                onCancel()
            }
        }.create().also {
            it.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    protected fun getInputText(): String? = binding?.inputLayout?.editText?.text?.toString()

    open fun setInitText(): String? = null

    @CallSuper
    override fun onCancel() {
        findNavController().navigateUp()
    }
}