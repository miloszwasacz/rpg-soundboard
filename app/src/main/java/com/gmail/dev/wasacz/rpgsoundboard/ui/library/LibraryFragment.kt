package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentRefreshableListBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.RefreshableListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song

class LibraryFragment : RefreshableListFragment<FragmentRefreshableListBinding, Song, LibraryViewModel>(
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
        val viewModel by activityViewModels<LibraryViewModel>()
        return viewModel
    }

    override fun List<Song>.initAdapter(): LibraryAdapter = LibraryAdapter(this)
    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)
}