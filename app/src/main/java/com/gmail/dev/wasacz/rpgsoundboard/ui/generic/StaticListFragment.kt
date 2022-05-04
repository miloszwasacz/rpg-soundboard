package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentListBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel.ListState
import com.gmail.dev.wasacz.rpgsoundboard.ui.hide
import com.gmail.dev.wasacz.rpgsoundboard.ui.show
import kotlinx.coroutines.flow.collectLatest

abstract class StaticListFragment<T, VM: ListViewModel<T>>(private val placeholder: Placeholder) :
    ListFragment<FragmentListBinding, T, VM>(FragmentListBinding::inflate) {
    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.run {
            placeholder = this@StaticListFragment.placeholder
            recyclerView.layoutManager = initLayoutManager()
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
        viewModel.onFragmentInit(requireContext())
        return binding.root
    }

    companion object {
        val timeoutPlaceholder: Placeholder
            get() = Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.placeholder_timeout_message)
        val errorPlaceholder: Placeholder
            get() = Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.placeholder_error_message)
    }
}