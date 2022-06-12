package com.gmail.dev.wasacz.rpgsoundboard.ui.library.songs

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.DialogAddSongsBinding
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentListBinding
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ListItemNewSongBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.*
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingFragment
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.MarginItemDecoration
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.SelectAdapter
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Song
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.TempLocalSong
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AddSongFragment : FullscreenDialogFragment() {
    private companion object {
        const val MENU_CONFIRM_ID = R.id.action_add
        val tabs = listOf(
            R.id.tab_saved,
            R.id.tab_device
        )
        val tabNames = mapOf(
            R.id.tab_saved to (R.string.tab_from_saved to R.drawable.ic_library_24dp),
            R.id.tab_device to (R.string.tab_from_device to R.drawable.ic_folder_open_24dp)
        )
        val placeholder = Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.app_name)
    }

    private lateinit var binding: DialogAddSongsBinding
    private lateinit var viewModel: NewSongViewModel
    private val navArgs by navArgs<AddSongFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val dbViewModel by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<NewSongViewModel> {
            NewSongViewModel.Factory(requireActivity().application, dbViewModel, navArgs.playlistId)
        }
        this.viewModel = viewModel
        binding = DialogAddSongsBinding.inflate(inflater, container, false)
        with(binding) {
            lifecycleOwner = viewLifecycleOwner
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    MENU_CONFIRM_ID -> {
                        lifecycleScope.launch {
                            getAdapters().forEach {
                                when (it) {
                                    is SavedAdapter -> viewModel.addSongs(it.getSelectedItems())
                                    is LocalAdapter -> viewModel.addSongs(it.getSelectedItems())
                                }
                            }
                            finish()
                        }
                        true
                    }
                    else -> false
                }
            }
        }
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
        binding.viewPager.adapter = TabAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val id = tabs[position]
            tab.id = id
            tabNames[id]?.let { (name, drawable) ->
                tab.setText(name)
                tab.setIcon(drawable)
            }
        }.attach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun toggleConfirmButton(enabled: Boolean) {
        binding.toolbar.menu.findItem(MENU_CONFIRM_ID)?.isEnabled = enabled
    }

    fun updateTabBadge(@IdRes id: Int?, itemCount: Int) {
        val tabIndex = id?.let { tabs.indexOf(it) }.takeIf { it != -1 } ?: return
        val tab = binding.tabLayout.getTabAt(tabIndex) ?: return
        if (itemCount > 0) tab.orCreateBadge.number = itemCount
        else tab.removeBadge()
    }

    private fun getAdapters(): List<Adapter<*>> = (binding.viewPager.adapter as? TabAdapter)?.getAllFragments()?.mapNotNull {
        it.getAdapter()
    } ?: listOf()

    inner class TabAdapter(private val fragment: AddSongFragment) : FragmentStateAdapter(fragment) {
        var areAllSelectedEmpty: Boolean = true
            set(value) {
                field = when (value) {
                    false -> value
                    true -> getAllFragments().all { it.getAdapter()?.getSelectedItems()?.isEmpty() ?: true }
                }
            }

        override fun getItemCount(): Int = tabs.size

        override fun createFragment(position: Int): Fragment = TabFragment.newInstance(tabs[position])

        fun getAllFragments(): List<TabFragment> = buildList {
            for (i in 0 until itemCount)
                fragment.childFragmentManager.findFragmentByTag("f$i").let { it as? TabFragment }?.let { add(it) }
        }
    }

    class TabFragment : DataBindingFragment<FragmentListBinding>(FragmentListBinding::inflate), ITabFragment {
        companion object {
            private const val TAB_ID_ARG = "TAB_ID_ARG"

            fun newInstance(@IdRes tabId: Int?): TabFragment {
                val fragment = TabFragment()
                fragment.arguments = Bundle().apply {
                    tabId?.let { putInt(TAB_ID_ARG, it) }
                }
                return fragment
            }
        }

        var tabId: Int? = null
            private set
        lateinit var parentFragment: AddSongFragment
            private set
        private lateinit var tabAdapter: TabAdapter

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            super.onCreateView(inflater, container, savedInstanceState)
            tabId = arguments?.getInt(TAB_ID_ARG)
            getReferences()

            val viewModel by viewModels<NewSongViewModel>({ parentFragment })
            with(binding) {
                placeholder = AddSongFragment.placeholder
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.addItemDecoration(MarginItemDecoration(R.dimen.card_margin))
                val isReady = when (tabId) {
                    R.id.tab_saved -> viewModel.savedSongs.value != null
                    R.id.tab_device -> viewModel.localSongs.value != null
                    else -> true
                }
                if (!isReady) progress.show()
            }
            lifecycleScope.launch {
                delay(resources.getDefaultAnimTimeLong(AnimTime.LONG))
                lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    when (tabId) {
                        R.id.tab_saved -> {
                            viewModel.savedSongs.filterNotNull().collect {
                                onList(it) { list -> SavedAdapter(list, this@TabFragment, tabAdapter) }
                            }
                        }
                        R.id.tab_device -> {
                            viewModel.localSongs.filterNotNull().collect {
                                onList(it) { list -> LocalAdapter(list, this@TabFragment, tabAdapter) }
                            }
                        }
                    }
                }
            }
            return binding.root
        }

        override fun getAdapter(): Adapter<*>? = binding.recyclerView.adapter as? Adapter<*>

        private fun getReferences() {
            val activity = activity?.let { it as? INavigationActivity }
                ?: throw ActivityNotFoundException("There was no activity present or it was of a wrong type.")
            parentFragment = activity.getCurrentFragment()?.let { it as? AddSongFragment }
                ?: throw InstantiationException("There was no fragment present or it was of a wrong type.")
            tabAdapter = parentFragment.binding.viewPager.adapter?.let { it as? TabAdapter }
                ?: throw InstantiationException("There was no tab adapter present or it was of a wrong type.")
        }

        private fun <T> onList(list: List<T>, adapterBuilder: (list: List<T>) -> Adapter<T>) {
            with(binding) {
                (progress as View).hide()
                if (list.isEmpty())
                    placeholderBinding.placeholderLayout.show()
                else {
                    recyclerView.adapter = adapterBuilder(list)
                    recyclerView.show()
                }
            }
        }
    }

    interface ITabFragment {
        fun getAdapter(): Adapter<*>?
    }

    abstract class Adapter<T>(
        list: List<T>,
        private val tabFragment: TabFragment,
        private val tabAdapter: TabAdapter
    ) : SelectAdapter<ListItemNewSongBinding, T>(
        list,
        ListItemNewSongBinding::inflate,
        tabFragment.parentFragment.binding.appbar,
        tabFragment.lifecycleScope
    ) {
        private val parentFragment get() = tabFragment.parentFragment

        override fun ListItemNewSongBinding.getClickableView(): View = cardView

        override fun ListItemNewSongBinding.setItemSelection(position: Int) {
            cardView.isChecked = isItemSelected(position)
            parentFragment.updateTabBadge(tabFragment.tabId, getSelectedItemsCount())
        }

        override fun toggleConfirmButton(enabled: Boolean) {
            tabAdapter.areAllSelectedEmpty = !enabled
            parentFragment.toggleConfirmButton(!tabAdapter.areAllSelectedEmpty)
        }
    }

    private class SavedAdapter(
        list: List<Song>,
        tabFragment: TabFragment,
        tabAdapter: TabAdapter
    ) : Adapter<Song>(list, tabFragment, tabAdapter) {
        override fun ListItemNewSongBinding.updateItemBindings(position: Int) {
            songName = list[position].title
        }
    }

    private class LocalAdapter(
        list: List<TempLocalSong>,
        tabFragment: TabFragment,
        tabAdapter: TabAdapter
    ) : Adapter<TempLocalSong>(list, tabFragment, tabAdapter) {
        override fun ListItemNewSongBinding.updateItemBindings(position: Int) {
            songName = list[position].name
        }
    }
}