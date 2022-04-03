package com.gmail.dev.wasacz.rpgsoundboard.ui.library

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.model.Item
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.RefreshableListFragment

class LibraryFragment : RefreshableListFragment<Item>(Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.app_name)) {
    override fun initViewModel(): LibraryViewModel {
        val viewModel by activityViewModels<LibraryViewModel>()
        return viewModel
    }

    override fun List<Item>.initAdapter(): LibraryAdapter = LibraryAdapter(this)
    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(this.context)
}