package com.gmail.dev.wasacz.rpgsoundboard.ui.library.playlists

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.Slide
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.DialogAddPlaylistsBinding
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemPlaylistAddBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.*
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.PlaylistItem
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreatePlaylistDialogFragment : SingleInputDialogFragment(
    R.string.dialog_title_new_playlist,
    R.string.hint_name,
    R.string.action_create
) {
    private lateinit var viewModel: PlaylistViewModel
    private val navArgs by navArgs<CreatePlaylistDialogFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbViewModel, navArgs.presetId) }
        this.viewModel = viewModel
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onConfirm() {
        getInputText()?.let {
            val name = it.trim()
            if (it.isNotBlank()) {
                lifecycleScope.launch {
                    val playlist = viewModel.createPlaylist(name)
                    requireDialog().dismiss()
                    val action = CreatePlaylistDialogFragmentDirections.navigationNewPlaylistToSongs(playlist, playlist.name)
                    findNavController().navigate(action)
                }
            }
        }
    }
}

class AddPlaylistsFragment : DialogFragment() {
    private companion object {
        const val MENU_CONFIRM_ID = R.id.action_add
    }
    private lateinit var binding: DialogAddPlaylistsBinding
    private lateinit var viewModel: PlaylistViewModel
    private val navArgs by navArgs<AddPlaylistsFragmentArgs>()
    private val placeholder = Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.app_name)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbViewModel, navArgs.presetId) }
        this.viewModel = viewModel
        binding = DialogAddPlaylistsBinding.inflate(inflater, container, false)
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
                                viewModel.addPlaylists(it.getSelectedItems())
                                findNavController().navigateUp()
                            }
                            true
                        } ?: false
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
            drawingViewId = R.id.nav_host_fragment
            startView = requireActivity().findViewById(R.id.main_fab)
            endView = binding.mainLayout
        }
        returnTransition = Slide().apply {
            addTarget(binding.mainLayout)
        }
        exitTransition = MaterialElevationScale(false)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun initList() {
        with(binding.listLayout) {
            placeholder = this@AddPlaylistsFragment.placeholder
            recyclerView.layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.playlist_span_count))
            recyclerView.addItemDecoration(
                MarginItemDecoration(
                    spaceSize = R.dimen.card_margin,
                    spanCount = resources.getInteger(R.integer.playlist_span_count)
                )
            )
        }
        lifecycleScope.launchWhenResumed {
            with(binding.listLayout) {
                (progress as View).show()
                val list = viewModel.getAllPlaylists()
                (progress as View).hide()
                if (list.isEmpty()) {
                    placeholderBinding.placeholderLayout.show()
                } else {
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

    override fun onResume() {
        super.onResume()
        if (viewModel.list.value.state.first != ListViewModel.ListState.READY) {
            lifecycleScope.launch {
                viewModel.refreshList(requireContext())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onFragmentPause()
    }

    class Adapter(list: List<PlaylistItem>, private val appBar: AppBarLayout, private val lifecycleScope: LifecycleCoroutineScope, private val toggleConfirmButton: (enabled: Boolean) -> Unit) :
        DataBindingListAdapter<ListItemPlaylistAddBinding, PlaylistItem>(list, ListItemPlaylistAddBinding::inflate) {
        private val selected = mutableSetOf<Int>()

        override fun onBindViewHolder(holder: ViewHolder<ListItemPlaylistAddBinding>, position: Int) {
            toggleConfirmButton(selected.size > 0)
            holder.binding.apply {
                cardView.setOnClickListener {
                    appBar.setLiftableOverrideEnabled(true)
                    selected.toggle(position)
                    notifyItemChanged(position)
                }
                cardView.isChecked = selected.contains(position)
                updateBindings {
                    playlist = list[position]
                }
            }
            lifecycleScope.launch {
                delay(10)
                appBar.setLiftableOverrideEnabled(false)
            }
        }

        fun getSelectedItems(): List<PlaylistItem> = selected.map { list[it] }
    }
}

class RemovePlaylistsFragment : AlertDialogFragment(
    title = R.string.dialog_title_remove_playlists,
    message = R.string.dialog_message_remove_playlists,
    icon = R.drawable.ic_delete_24dp,
    confirmButtonText = R.string.action_remove
) {
    private lateinit var viewModel: PlaylistViewModel
    private val navArgs by navArgs<RemovePlaylistsFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbViewModel, navArgs.presetId) }
        this.viewModel = viewModel
        return dialog
    }

    override fun onConfirm() {
        setNavigationResult(R.string.nav_arg_remove_playlists_result, true)
        findNavController().navigateUp()
    }
}

class DeletePlaylistsFragment : AlertDialogFragment(
    title = R.string.dialog_title_delete_playlists,
    message = R.string.dialog_message_delete_playlists,
    icon = R.drawable.ic_delete_forever_24dp,
    confirmButtonText = R.string.action_delete
) {
    private lateinit var viewModel: PlaylistViewModel
    private val navArgs by navArgs<DeletePlaylistsFragmentArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlaylistViewModel> { PlaylistViewModel.Factory(dbViewModel, navArgs.presetId) }
        this.viewModel = viewModel
        return dialog
    }

    override fun onConfirm() {
        setNavigationResult(R.string.nav_arg_delete_playlists_result, true)
        findNavController().navigateUp()
    }
}