package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentLibraryBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ContextMenuFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectableItemListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PresetFragment : ContextMenuFragment<FragmentLibraryBinding, Preset, PresetViewModel>(
    R.menu.library_simple_context_menu,
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ),
    FragmentLibraryBinding::inflate,
    { context ->
        listOf(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }
), IToolbarFragment {
    //#region Context menu
    override val navigationGroup: List<Int> = listOf(
        R.id.navigation_library_presets,
        R.id.navigation_dialog_delete_presets
    )

    override fun getAdapter(): PresetAdapter? = binding.listLayout.recyclerView.adapter as? PresetAdapter
    override fun onActionItemClicked(itemId: Int?): Boolean = when (itemId) {
        R.id.action_delete -> {
            val action = PresetFragmentDirections.navigationLibraryDeletePresets()
            findNavController().navigate(action)
            true
        }
        else -> false
    }

    override fun onPrepareActionMode(): Boolean {
        lifecycleScope.launch {
            delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
            hideFAB()
        }
        return super.onPrepareActionMode()
    }

    override fun onDestroyActionMode() {
        super.onDestroyActionMode()
        showFAB()
    }
    //#endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            toolbar.setupDefault(findNavController(), activity)
            listLayout.recyclerView.viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
            inflateList(listLayout)
        }
        setupFAB(R.drawable.ic_add_24dp, R.string.action_create) {
            val state = viewModel.list.value.state.first
            if (state == ListViewModel.ListState.READY || state == ListViewModel.ListState.EMPTY) {
                val action = PresetFragmentDirections.navigationLibraryNewPreset()
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNavigationResult<Boolean>(R.id.navigation_library_presets, R.string.nav_arg_delete_presets_result) { result ->
            if (result) {
                getAdapter()?.let {
                    viewModel.viewModelScope.launch {
                        viewModel.deletePresets(it.getSelectedItems())
                        viewModel.refreshList(requireContext())
                        it.finishActionMode()
                    }
                }
            }
        }
    }

    override fun initViewModel(): PresetViewModel {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbViewModel) }
        return viewModel
    }

    override fun List<Preset>.initAdapter(): SelectableItemListAdapter<Preset> = PresetAdapter(
        this,
        findNavController(),
        binding.toolbar,
        { startActionMode() },
        ::setItemClickedExitAnimation
    )

    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)
    override fun getToolbar(): MaterialToolbar = binding.toolbar

    //#region Transitions
    override fun onResume() {
        super.onResume()
        setDefaultExitAnimation()
    }

    private fun setItemClickedExitAnimation() {
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
    }

    private fun setDefaultExitAnimation() {
        exitTransition = MaterialFadeThrough()
        reenterTransition = null
    }
    //#endregion
}