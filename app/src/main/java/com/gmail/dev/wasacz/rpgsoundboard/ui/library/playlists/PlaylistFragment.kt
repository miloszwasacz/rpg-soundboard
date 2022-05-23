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
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.MarginItemDecoration
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaylistFragment : ContextMenuFragment<FragmentLibraryPresetBinding, PlaylistItem, PlaylistViewModel>(
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
    private val navArgs by navArgs<PlaylistFragmentArgs>()

    //#region Context menu
    override val navigationGroup: List<Int> = listOf(
        R.id.navigation_library_playlists,
        R.id.navigation_dialog_remove_playlists,
        R.id.navigation_dialog_delete_playlists
    )

    override fun getAdapter(): PlaylistAdapter? = binding.listLayout.recyclerView.adapter as? PlaylistAdapter

    override fun onActionItemClicked(itemId: Int?): Boolean = when (itemId) {
        R.id.action_remove -> {
            val action = PlaylistFragmentDirections.navigationLibraryRemovePlaylists(navArgs.presetId)
            findNavController().navigate(action)
            true
        }
        R.id.action_delete -> {
            val action = PlaylistFragmentDirections.navigationLibraryDeletePlaylists(navArgs.presetId)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply { drawingViewId = R.id.nav_host_fragment }
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            presetId = navArgs.presetId
            listLayout.recyclerView.viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
            toolbar.setupDefault(findNavController(), activity)
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    //TODO Add Spotify playlist
                    R.id.action_create -> {
                        val action = PlaylistFragmentDirections.navigationLibraryNewPlaylist(navArgs.presetId)
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
            inflateList(listLayout)
        }
        setupFAB(R.drawable.ic_playlist_add_24dp, R.string.action_add) {
            val action = PlaylistFragmentDirections.navigationLibraryAddPlaylists(navArgs.presetId)
            findNavController().navigate(action, binding.toolbar)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNavigationResult<Boolean>(R.id.navigation_library_playlists, R.string.nav_arg_remove_playlists_result) { result ->
            if (result) {
                getAdapter()?.let {
                    viewModel.viewModelScope.launch {
                        viewModel.removePlaylists(it.getSelectedItems())
                        it.notifyItemsRemoved()
                        it.finishActionMode()
                        delay(resources.getDefaultAnimTimeLong(AnimTime.LONG))
                        viewModel.refreshList(requireContext())
                    }
                }
            }
        }
        getNavigationResult<Boolean>(R.id.navigation_library_playlists, R.string.nav_arg_delete_playlists_result) { result ->
            if (result) {
                getAdapter()?.let {
                    viewModel.viewModelScope.launch {
                        viewModel.deletePlaylists(it.getSelectedItems())
                        it.notifyItemsRemoved()
                        it.finishActionMode()
                        delay(resources.getDefaultAnimTimeLong(AnimTime.LONG))
                        viewModel.refreshList(requireContext())
                    }
                }
            }
        }
    }

    override fun initViewModel(): PlaylistViewModel {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbViewModel, navArgs.presetId) }
        return viewModel
    }

    override fun List<PlaylistItem>.initAdapter(): PlaylistAdapter =
        PlaylistAdapter(this, findNavController(), binding.toolbar) { startActionMode() }

    override fun initLayoutManager(): RecyclerView.LayoutManager =
        GridLayoutManager(context, resources.getInteger(R.integer.playlist_span_count))

    override fun getToolbar(): MaterialToolbar = binding.toolbar
}