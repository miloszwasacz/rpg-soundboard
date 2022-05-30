package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentLibraryPresetBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ContextMenuFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.MarginItemDecoration
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PresetFragment : ContextMenuFragment<FragmentLibraryPresetBinding, PlaylistItem, PresetViewModel>(
    R.menu.library_context_menu,
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ),
    FragmentLibraryPresetBinding::inflate,
    { context ->
        listOf(
            MarginItemDecoration(
                spaceSize = R.dimen.card_margin,
                spanCount = context.resources.getInteger(R.integer.playlist_span_count)
            )
        )
    }
), IToolbarFragment {
    private val navArgs by navArgs<PresetFragmentArgs>()

    //#region Context menu
    override val navigationGroup: List<Int> = listOf(
        R.id.navigation_library_preset,
        R.id.navigation_dialog_remove_playlists,
        R.id.navigation_dialog_delete_playlists
    )

    override fun getAdapter(): PlaylistAdapter? = binding.listLayout.recyclerView.adapter as? PlaylistAdapter

    override fun onActionItemClicked(itemId: Int?): Boolean = when (itemId) {
        R.id.action_remove -> {
            val action = PresetFragmentDirections.navigationLibraryRemovePlaylists(navArgs.presetId)
            findNavController().navigate(action)
            true
        }
        R.id.action_delete -> {
            val action = PresetFragmentDirections.navigationLibraryDeletePlaylists(navArgs.presetId)
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
            presetId = navArgs.presetId
            listLayout.recyclerView.viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
            toolbar.setupDefault(this@PresetFragment, navArgs.presetName)
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    //TODO Add Spotify playlist
                    R.id.action_create -> {
                        val action = PresetFragmentDirections.navigationLibraryNewPlaylist(navArgs.presetId)
                        findNavController().navigate(action)
                        true
                    }
                    R.id.action_rename -> {
                        val action = PresetFragmentDirections.navigationLibraryRenamePreset(navArgs.presetName)
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
            inflateList(listLayout)
        }
        setupFAB(R.drawable.ic_playlist_add_24dp, R.string.action_add) {
            val state = viewModel.list.value.state.first
            if (state == ListViewModel.ListState.READY || state == ListViewModel.ListState.EMPTY) {
                val action = PresetFragmentDirections.navigationLibraryAddPlaylists(navArgs.presetId)
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTransitions {
            enterTransition = MaterialFadeThrough()
            exitTransition = MaterialElevationScale(false)
            reenterTransition = MaterialElevationScale(true)
        }
        getNavigationResult<Boolean>(R.id.navigation_library_preset, R.string.nav_arg_remove_playlists_result) { result ->
            if (result) {
                getAdapter()?.let {
                    viewModel.viewModelScope.launch {
                        viewModel.removePlaylists(it.getSelectedItems())
                        viewModel.refreshList(requireContext())
                        it.finishActionMode()
                    }
                }
            }
        }
        getNavigationResult<Boolean>(R.id.navigation_library_preset, R.string.nav_arg_delete_playlists_result) { result ->
            if (result) {
                getAdapter()?.let {
                    viewModel.viewModelScope.launch {
                        viewModel.deletePlaylists(it.getSelectedItems())
                        viewModel.refreshList(requireContext())
                        it.finishActionMode()
                    }
                }
            }
        }
        getNavigationResult<String>(R.id.navigation_library_preset, R.string.nav_arg_rename_preset_result) { result ->
            if (result != navArgs.presetName) {
                viewModel.viewModelScope.launch {
                    viewModel.renamePreset(result)
                    val action = PresetFragmentDirections.refreshLibraryPreset(navArgs.presetId, result)
                    refresh(action)
                }
            }
        }
    }

    override fun initViewModel(): PresetViewModel {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbViewModel, navArgs.presetId) }
        return viewModel
    }

    override fun List<PlaylistItem>.initAdapter(): PlaylistAdapter = PlaylistAdapter(this, this@PresetFragment) {
        startActionMode()
    }

    override fun initLayoutManager(): RecyclerView.LayoutManager =
        GridLayoutManager(context, resources.getInteger(R.integer.playlist_span_count))

    override fun getToolbar(): MaterialToolbar = binding.toolbar
}