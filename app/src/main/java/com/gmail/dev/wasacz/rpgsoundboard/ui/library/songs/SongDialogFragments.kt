package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.DialogAddLocalSongsBinding
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemLocalStorageSongBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.MarginItemDecoration
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectAdapter
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.TempLocalSong
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.launch

class AddLocalSongFragment : DialogFragment(), ICustomTransitionFragment {
    private companion object {
        const val MENU_CONFIRM_ID = R.id.action_add
    }

    private lateinit var binding: DialogAddLocalSongsBinding
    private lateinit var viewModel: LocalSongViewModel
    private val navArgs by navArgs<AddLocalSongFragmentArgs>()
    private val placeholder = Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.app_name)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<LocalSongViewModel> { LocalSongViewModel.Factory(dbViewModel) }
        this.viewModel = viewModel
        binding = DialogAddLocalSongsBinding.inflate(inflater, container, false)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            listLayout.recyclerView.viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    MENU_CONFIRM_ID -> {
                        getAdapter()?.let {
                            lifecycleScope.launch {
                                viewModel.addSongs(it.getSelectedItems(), navArgs.playlistId)
                                findNavController().navigateUp()
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
        }
        initList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        enterTransition = MaterialContainerTransform().apply {
            startView = requireActivity().findViewById(R.id.main_fab)
            endView = binding.mainLayout
        }
        returnTransition = Slide().apply {
            addTarget(binding.mainLayout)
        }
        exitTransition = MaterialElevationScale(false)
        super.onViewCreated(view, savedInstanceState)
        hideNavView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun initList() {
        with(binding.listLayout) {
            placeholder = this@AddLocalSongFragment.placeholder
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.addItemDecoration(MarginItemDecoration(R.dimen.card_margin))
        }
        lifecycleScope.launchWhenResumed {
            with(binding.listLayout) {
                (progress as View).show()
                val list = viewModel.getSongsFromDevice(requireContext())
                (progress as View).hide()
                if (list.isEmpty())
                    placeholderBinding.placeholderLayout.show()
                else {
                    recyclerView.adapter = Adapter(list, binding.appbar, lifecycleScope, ::toggleConfirmButton)
                    recyclerView.show()
                }
            }
        }
    }

    private fun toggleConfirmButton(enabled: Boolean) {
        binding.toolbar.menu.findItem(MENU_CONFIRM_ID)?.isEnabled = enabled
    }

    private fun getAdapter(): Adapter? = binding.listLayout.recyclerView.adapter as? Adapter

    override fun onPause() {
        super.onPause()
        showNavView()
    }

    class Adapter(
        list: List<TempLocalSong>,
        appBar: AppBarLayout,
        lifecycleScope: LifecycleCoroutineScope,
        toggleConfirmButton: (Boolean) -> Unit
    ) : SelectAdapter<ListItemLocalStorageSongBinding, TempLocalSong>(
        list,
        ListItemLocalStorageSongBinding::inflate,
        appBar,
        lifecycleScope,
        toggleConfirmButton
    ) {
        override fun ListItemLocalStorageSongBinding.getClickableView(): View = cardView

        override fun ListItemLocalStorageSongBinding.setItemSelection(position: Int) {
            cardView.isChecked = isItemSelected(position)
        }

        override fun ListItemLocalStorageSongBinding.updateItemBindings(position: Int) {
            song = list[position]
        }
    }
}