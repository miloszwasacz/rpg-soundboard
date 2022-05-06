package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentRefreshableListBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.RefreshableListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset

class LibraryFragment : RefreshableListFragment<FragmentRefreshableListBinding, Playlist, LibraryViewModel>(
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ), FragmentRefreshableListBinding::inflate
) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        inflateList(binding)
        return binding.root
    }

    override fun initViewModel(): LibraryViewModel {
        val dbVM by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<LibraryViewModel> { LibraryViewModel.Factory(dbVM) }
        return viewModel
    }

    override fun List<Playlist>.initAdapter(): LibraryAdapter = LibraryAdapter(this)
    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)
}