package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentListBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel.ListState
import com.gmail.dev.wasacz.rpgsoundboard.ui.hide
import com.gmail.dev.wasacz.rpgsoundboard.ui.show
import kotlinx.coroutines.flow.collectLatest

abstract class StaticListFragment<B : ViewDataBinding, T, VM : ListViewModel<T>>(
    private val placeholder: Placeholder,
    inflate: DataBindingInflate<B>,
    private val itemDecoration: ((context: Context) -> List<RecyclerView.ItemDecoration>)? = {
        listOf(MarginItemDecoration(spaceSize = R.dimen.card_margin))
    }
) : ListFragment<B, T, VM>(inflate) {

    @CallSuper
    protected fun inflateList(binding: FragmentListBinding) {
        with(binding) {
            placeholder = this@StaticListFragment.placeholder
            recyclerView.layoutManager = initLayoutManager()
            itemDecoration?.let {
                it(recyclerView.context).forEach { decoration ->
                    recyclerView.addItemDecoration(decoration)
                }
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.list.collectLatest { (list, statePair) ->
                val (state, message) = statePair
                binding.apply {
                    recyclerView.adapter = list?.run { initAdapter() }
                    placeholderErrorCode = message ?: when (state) {
                        ListState.SERIALIZATION_ERROR,
                        ListState.INTERNAL_ERROR -> state.name
                        else -> null
                    }
                    with(placeholderBinding) {
                        when (state) {
                            ListState.LOADING -> {
                                placeholderLayout.hide()
                                recyclerView.hide(View.INVISIBLE)
                                (progress as View).show()
                            }
                            ListState.READY -> {
                                placeholderLayout.hide()
                                progress.hide()
                                recyclerView.show()
                            }
                            ListState.EMPTY -> {
                                placeholder = this@StaticListFragment.placeholder
                                recyclerView.hide(View.INVISIBLE)
                                (progress as View).hide()
                                placeholderLayout.show()
                            }
                            ListState.SERIALIZATION_ERROR,
                            ListState.INTERNAL_ERROR -> {
                                placeholder = errorPlaceholder
                                recyclerView.hide(View.INVISIBLE)
                                (progress as View).hide()
                                placeholderLayout.show()
                            }
                            ListState.TIMEOUT -> {
                                placeholder = timeoutPlaceholder
                                recyclerView.hide(View.INVISIBLE)
                                (progress as View).hide()
                                placeholderLayout.show()
                            }
                        }
                    }
                }
            }
        }
        viewModel.onFragmentInit(requireContext())
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (binding is FragmentListBinding)
            (binding as FragmentListBinding).paddingTop = resources.getDimension(R.dimen.scroll_view_padding_top)
        return binding.root
    }
}