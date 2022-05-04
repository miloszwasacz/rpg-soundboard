package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.graphics.Paint
import android.view.View
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentRefreshableListBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel.ListState
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment.Companion.errorPlaceholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment.Companion.timeoutPlaceholder
import com.google.android.material.R.attr.colorOnSecondaryContainer
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.SurfaceColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class RefreshableListFragment<B : ViewDataBinding, T, VM : ListViewModel<T>>(
    private val placeholder: Placeholder,
    inflate: DataBindingInflate<B>
) : ListFragment<B, T, VM>(inflate) {
    private var currentState: ListState? = null

    @CallSuper
    protected fun inflateList(binding: FragmentRefreshableListBinding) {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            placeholder = this@RefreshableListFragment.placeholder
            listLayout.recyclerView.layoutManager = initLayoutManager()
            swipeLayout.apply {
                val indicatorOffset =
                    resources.getSummedDimensionPixelOffset(R.dimen.scroll_view_padding_top, R.dimen.refresh_indicator_offset)
                setProgressViewEndTarget(true, indicatorOffset)
                setOnRefreshListener {
                    refresh()
                }
                @Suppress("PrivateResource")
                setStyle(
                    MaterialColors.getColor(this, colorOnSecondaryContainer),
                    SurfaceColors.SURFACE_4.getColor(this.context),
                    Paint.Cap.ROUND
                )
            }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.list.collectLatest { (list, statePair) ->
                val (state, message) = statePair
                binding.listLayout.apply {
                    launch {
                        recyclerView.adapter = list.let {
                            if (it == null) delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                            it?.initAdapter()
                        }
                    }
                    if (state != currentState) {
                        placeholderErrorCode = message ?: when (state) {
                            ListState.SERIALIZATION_ERROR,
                            ListState.INTERNAL_ERROR -> state.name
                            else -> null
                        }
                        when (state) {
                            ListState.LOADING -> {
                                binding.swipeLayout.isRefreshing = true
                                placeholderLayout.hide()
                                recyclerView.hide(View.INVISIBLE)
                            }
                            ListState.READY -> {
                                binding.swipeLayout.isRefreshing = false
                                placeholderLayout.hide()
                                recyclerView.show()
                            }
                            ListState.EMPTY -> {
                                placeholder = this@RefreshableListFragment.placeholder
                                binding.swipeLayout.isRefreshing = false
                                recyclerView.hide(View.INVISIBLE)
                                placeholderLayout.show()
                            }
                            ListState.TIMEOUT -> {
                                placeholder = timeoutPlaceholder
                                binding.swipeLayout.isRefreshing = false
                                recyclerView.hide(View.INVISIBLE)
                                placeholderLayout.show()
                            }
                            ListState.SERIALIZATION_ERROR,
                            ListState.INTERNAL_ERROR -> {
                                placeholder = errorPlaceholder
                                binding.swipeLayout.isRefreshing = false
                                recyclerView.hide(View.INVISIBLE)
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
    protected fun refresh() {
        viewModel.fetchList(requireContext(), delay = resources.getDefaultAnimTimeLong(AnimTime.LONG))
    }
}