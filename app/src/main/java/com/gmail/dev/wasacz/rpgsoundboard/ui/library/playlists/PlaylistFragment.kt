package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentLibraryPresetBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.MarginItemDecoration
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale

class PlaylistFragment : StaticListFragment<FragmentLibraryPresetBinding, PlaylistItem, PlaylistViewModel>(
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
) {
    private val navArgs by navArgs<PlaylistFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = null
        sharedElementEnterTransition = MaterialContainerTransform().apply { drawingViewId = R.id.main_layout }
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
            toolbar.setupWithNavController(findNavController())
            inflateList(listLayout)
        }
        return binding.root
    }

    override fun initViewModel(): PlaylistViewModel {
        val dbVm by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbVm, navArgs.presetId) }
        return viewModel
    }

    override fun List<PlaylistItem>.initAdapter(): PlaylistAdapter = PlaylistAdapter(this, findNavController())
    override fun initLayoutManager(): RecyclerView.LayoutManager =
        GridLayoutManager(context, resources.getInteger(R.integer.playlist_span_count))
}