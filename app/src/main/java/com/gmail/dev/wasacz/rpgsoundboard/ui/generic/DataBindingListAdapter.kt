package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.ui.toggle
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

abstract class SelectAdapter<B: ViewDataBinding, T>(
    list: List<T>,
    itemInflate: DataBindingInflate<B>,
    private val appBar: AppBarLayout,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val toggleConfirmButton: (enabled: Boolean) -> Unit
): DataBindingListAdapter<B, T>(list, itemInflate) {
    private val selected = mutableSetOf<Int>()

    override fun onBindViewHolder(holder: ViewHolder<B>, position: Int) {
        toggleConfirmButton(selected.size > 0)
        with(holder.binding) {
            getClickableView().setOnClickListener {
                appBar.setLiftableOverrideEnabled(true)
                selected.toggle(position)
                notifyItemChanged(position)
            }
            setItemSelection(position)
            updateBindings {
                updateItemBindings(position)
            }
        }
        lifecycleScope.launch {
            delay(10)
            appBar.setLiftableOverrideEnabled(false)
        }
    }

    abstract fun B.getClickableView(): View
    abstract fun B.setItemSelection(position: Int)
    abstract fun B.updateItemBindings(position: Int)

    protected fun isItemSelected(position: Int): Boolean = selected.contains(position)

    fun getSelectedItems(): List<T> = selected.map { list[it] }
}