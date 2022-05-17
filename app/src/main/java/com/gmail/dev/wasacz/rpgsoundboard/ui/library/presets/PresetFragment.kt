package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentLibraryBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectableItemListAdapter
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PresetFragment : StaticListFragment<FragmentLibraryBinding, Preset, PresetViewModel>(
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ),
    FragmentLibraryBinding::inflate,
    { context ->
        listOf(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }
), IToolbarFragment {
    private val destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when (destination.id) {
            R.id.navigation_library_presets,
            R.id.navigation_dialog_delete_presets -> {
            }
            else -> binding.listLayout.recyclerView.adapter?.let {
                if (it is PresetAdapter) it.finishActionMode()
            }
        }
    }
    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = mode?.run {
            menuInflater?.inflate(R.menu.library_context_menu, menu) ?: return@run null
            binding.listLayout.recyclerView.adapter?.let {
                if (it is PresetAdapter) it.onCreateActionMode()
            } ?: return@run null
            true
        } ?: false

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            lifecycleScope.launch {
                delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                hideFAB()
            }
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = when (item?.itemId) {
            R.id.action_delete -> {
                val action = PresetFragmentDirections.navigationLibraryDeletePresets()
                findNavController().navigate(action)
                true
            }
            else -> false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            binding.listLayout.recyclerView.adapter?.let {
                if (it is PresetAdapter) it.onDestroyActionMode()
            }
            showFAB()
        }
    }

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
        setupFAB(R.drawable.ic_add_24dp) {
            val action = PresetFragmentDirections.navigationLibraryAddPreset()
            findNavController().navigate(action)
        }
        findNavController().addOnDestinationChangedListener(destinationChangedListener)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNavigationResult<Boolean>(R.id.navigation_library_presets, R.string.nav_arg_delete_presets_result) { result ->
            if (result) {
                binding.listLayout.recyclerView.adapter?.let {
                    if (it is PresetAdapter) {
                        viewModel.viewModelScope.launch {
                            viewModel.deletePresets(it.getSelectedItems())
                            it.notifyItemsRemoved()
                            it.finishActionMode()
                            delay(resources.getDefaultAnimTimeLong(AnimTime.LONG))
                            viewModel.refreshList(requireContext())
                        }
                    }
                }
            }
        }
    }

    override fun initViewModel(): PresetViewModel {
        val dbVM by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbVM) }
        return viewModel
    }

    override fun List<Preset>.initAdapter(): SelectableItemListAdapter<Preset> = PresetAdapter(
        this,
        findNavController(),
        binding.toolbar,
        { activity?.startActionMode(actionModeCallback) },
        ::setItemClickedExitAnimation
    )

    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)
    override fun getToolbar(): MaterialToolbar = binding.toolbar

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

    override fun onStop() {
        findNavController().removeOnDestinationChangedListener(destinationChangedListener)
        super.onStop()
    }
}