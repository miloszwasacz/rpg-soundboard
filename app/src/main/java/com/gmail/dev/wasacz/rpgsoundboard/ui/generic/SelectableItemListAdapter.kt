package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemSelectableBinding

open class SelectableItemListAdapter<T>(
    list: List<T>,
    private val getText: (T) -> String,
    private val onItemClickCallback: ((item: T, binding: ListItemSelectableBinding) -> Unit)? = null,
    private val selectOnLongClick: Boolean = false
) : DataBindingListAdapter<ListItemSelectableBinding, T>(list, ListItemSelectableBinding::inflate) {
    protected open val selected = mutableSetOf<Int>()

    override fun onBindViewHolder(holder: ViewHolder<ListItemSelectableBinding>, position: Int) {
        holder.binding.apply {
            val item = list[position]
            itemLayout.setOnClickListener {
                if (!onItemClick(item, this))
                    onItemClickCallback?.invoke(item, this) ?: selectItem(this, position)
            }
            if (onItemClickCallback != null || selectOnLongClick) {
                itemLayout.setOnLongClickListener {
                    selectItem(this, position)
                    true
                }
            }
            updateBindings {
                text = getText(item)
                isSelectable = true
                isSelected = selected.contains(position)
            }
        }
    }

    /**
     * Override this function if you want to implement custom item click logic (return true to skip [onItemClickCallback] invocation).
     */
    protected open fun onItemClick(item: T, binding: ListItemSelectableBinding): Boolean = false

    protected open fun selectItem(position: Int) {
        if (selected.contains(position)) selected.remove(position)
        else selected.add(position)
    }

    protected open fun selectItem(binding: ListItemSelectableBinding, position: Int) {
        with(binding) {
            if (isSelectable != false) {
                selectItem(position)

                updateBindings {
                    isSelected = selected.contains(position)
                }
            }
        }
    }

    fun getSelectedItems(): List<T> = selected.map { list[it] }
}