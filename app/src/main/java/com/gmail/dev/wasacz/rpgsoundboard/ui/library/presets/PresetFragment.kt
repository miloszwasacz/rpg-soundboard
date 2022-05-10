package com.gmail.dev.wasacz.rpgsoundboard.ui.library.presets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentLibraryBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.StaticListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Preset
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough

class PresetFragment : StaticListFragment<FragmentLibraryBinding, Preset, PresetViewModel>(
    Placeholder(
        R.drawable.ic_dashboard_black_24dp,
        R.string.app_name
    ),
    FragmentLibraryBinding::inflate,
    { context ->
        listOf(DividerItemDecoration(context, RecyclerView.VERTICAL))
    }
) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            toolbar.setupWithNavController(findNavController())
            listLayout.recyclerView.viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
            inflateList(listLayout)
        }
        return binding.root
    }

    override fun initViewModel(): PresetViewModel {
        val dbVM by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PresetViewModel> { PresetViewModel.Factory(dbVM) }
        return viewModel
    }

    override fun List<Preset>.initAdapter(): PresetAdapter = PresetAdapter(this, findNavController(), ::setItemClickedExitAnimation)
    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)

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
}