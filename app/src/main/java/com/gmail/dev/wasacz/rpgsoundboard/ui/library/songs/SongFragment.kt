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
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.setupDefault
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import com.google.android.material.transition.MaterialContainerTransform

class SongFragment : StaticListFragment<FragmentLibraryPlaylistBinding, Song, SongViewModel>(
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ),
    FragmentLibraryPlaylistBinding::inflate
) {
    private val navArgs by navArgs<SongFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = null
        sharedElementEnterTransition = MaterialContainerTransform().apply { drawingViewId = R.id.nav_host_fragment }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            playlistId = navArgs.playlistItem.id
            toolbarLayout.setupDefault(context, toolbar, findNavController(), activity)
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
        return binding.root
    }

    override fun initViewModel(): SongViewModel {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<SongViewModel> { SongViewModel.Factory(dbViewModel, navArgs.playlistItem) }
        return viewModel
    }

    override fun List<Song>.initAdapter(): SongAdapter = SongAdapter(this)
    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)
}