package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentLibraryPlaylistBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ListViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough

class PlaylistFragment : StaticListFragment<FragmentLibraryPlaylistBinding, Song, PlaylistViewModel>(
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ),
    FragmentLibraryPlaylistBinding::inflate
), IToolbarFragment {
    private val navArgs by navArgs<PlaylistFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            playlistId = navArgs.playlistItem.id
            toolbarLayout.setupDefault(toolbar, this@PlaylistFragment, navArgs.playlistName)
            //TODO Playlist background image
            toolbarBackground.setImageResource(R.drawable.ic_launcher_background)
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_rename -> {
                        //TODO Rename playlist
                        true
                    }
                    R.id.action_manage -> {
                        //TODO Open contextual action bar
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
                val action = PlaylistFragmentDirections.navigationLibraryAddLocalSongs(navArgs.playlistItem.id)
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
    }

    override fun initViewModel(): PlaylistViewModel {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbViewModel, navArgs.playlistItem) }
        return viewModel
    }

    override fun List<Song>.initAdapter(): SongAdapter = SongAdapter(this)
    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)
    override fun getToolbar(): MaterialToolbar = binding.toolbar
    override fun getToolbarLayout(): CollapsingToolbarLayout = binding.toolbarLayout
}