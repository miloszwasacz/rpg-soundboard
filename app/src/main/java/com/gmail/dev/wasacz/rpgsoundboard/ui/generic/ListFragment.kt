package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

data class Placeholder(@DrawableRes val drawable: Int?, @StringRes val text: Int?)

abstract class ListFragment<B : ViewDataBinding, T, VM: ListViewModel<T>>(inflate: DataBindingInflate<B>) : DataBindingFragment<B>(inflate) {
    protected lateinit var viewModel: VM

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel = initViewModel()
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    abstract fun initViewModel(): VM
    protected abstract fun List<T>.initAdapter(): RecyclerView.Adapter<*>
    protected abstract fun initLayoutManager(): RecyclerView.LayoutManager
}