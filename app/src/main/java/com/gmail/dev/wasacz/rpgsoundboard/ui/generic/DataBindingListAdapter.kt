package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class DataBindingListAdapter<B : ViewDataBinding, T>(protected var list: List<T>, private val itemInflate: DataBindingInflate<B>) :
    RecyclerView.Adapter<DataBindingListAdapter.ViewHolder<B>>() {
    class ViewHolder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> =
        ViewHolder(itemInflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = list.size
}

inline fun <T : ViewDataBinding> T.updateBindings(bindings: T.() -> Unit) {
    bindings(this)
    executePendingBindings()
}