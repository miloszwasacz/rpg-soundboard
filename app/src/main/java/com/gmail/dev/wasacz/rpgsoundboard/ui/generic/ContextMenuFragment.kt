package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R

abstract class ContextMenuFragment<B : ViewDataBinding, T, VM : ListViewModel<T>>(
    @MenuRes private val contextMenu: Int,
    placeholder: Placeholder,
    inflate: DataBindingInflate<B>,
    itemDecoration: ((context: Context) -> List<RecyclerView.ItemDecoration>)? = {
        listOf(MarginItemDecoration(spaceSize = R.dimen.card_margin))
    }
) : StaticListFragment<B, T, VM>(placeholder, inflate, itemDecoration) {
    /**
     * List of navigation ids that won't close the context menu when navigated to.
     * Should contain main fragment's id and ids of all dialog fragments opened by the context menu.
     */
    protected abstract val navigationGroup: List<Int>

    private val destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        if (destination.id !in navigationGroup)
            getAdapter()?.finishActionMode()
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        findNavController().addOnDestinationChangedListener(destinationChangedListener)
    }

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = mode?.run {
            menuInflater?.inflate(contextMenu, menu) ?: return@run null
            onCreateActionMode() ?: return@run null
            true
        } ?: false

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = onPrepareActionMode()

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = onActionItemClicked(item?.itemId)

        override fun onDestroyActionMode(mode: ActionMode?) {
            onDestroyActionMode()
        }
    }

    abstract fun getAdapter(): IContextMenuAdapter?

    open fun onCreateActionMode(): Any? = getAdapter()?.onCreateActionMode()

    open fun onPrepareActionMode(): Boolean = false

    abstract fun onActionItemClicked(@IdRes itemId: Int?): Boolean

    open fun onDestroyActionMode() {
        getAdapter()?.onDestroyActionMode()
    }

    fun startActionMode(): ActionMode? = activity?.startActionMode(actionModeCallback)

    @CallSuper
    override fun onStop() {
        findNavController().removeOnDestinationChangedListener(destinationChangedListener)
        super.onStop()
    }
}

interface IContextMenuAdapter {
    val startActionMode: () -> ActionMode?
    var actionMode: ActionMode?

    fun onCreateActionMode()
    fun onDestroyActionMode()
    fun finishActionMode() {
        actionMode?.finish()
    }
}
