package com.gmail.dev.wasacz.rpgsoundboard.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentPlayerBinding
import com.gmail.dev.wasacz.rpgsoundboard.services.MediaPlayerService
import com.gmail.dev.wasacz.rpgsoundboard.ui.DatabaseViewModel
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.Placeholder
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.RefreshableListFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.Playlist

class PlayerFragment :
    RefreshableListFragment<FragmentPlayerBinding, Playlist, PlayerViewModel>(
        Placeholder(R.drawable.ic_dashboard_black_24dp, R.string.app_name),
        FragmentPlayerBinding::inflate
    ) {
    private lateinit var playerService: MediaPlayerService
    private var serviceBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MediaPlayerService.LocalBinder
            playerService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }
    private val destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        if (serviceBound && destination.id != R.id.navigation_player) {
            activity?.apply {
                unbindService(connection)
                stopService(Intent(applicationContext, MediaPlayerService::class.java))
            }
        }
    }

    override fun initViewModel(): PlayerViewModel {
        val dbVM by activityViewModels<DatabaseViewModel>()
        val viewModel by viewModels<PlayerViewModel> { PlayerViewModel.Factory(dbVM) }
        return viewModel
    }

    override fun List<Playlist>.initAdapter(): PlaylistAdapter = PlaylistAdapter(this, this@PlayerFragment::playPlaylist)

    override fun initLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(context)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.run {
            inflateList(listFragment)
            button.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireActivity().apply {
            Intent(applicationContext, MediaPlayerService::class.java).also {
                bindService(it, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findNavController().addOnDestinationChangedListener(destinationChangedListener)
    }

    private fun playPlaylist(playlist: Playlist) {
        if (serviceBound) playerService.play(playlist)
    }

    override fun onPause() {
        findNavController().removeOnDestinationChangedListener(destinationChangedListener)
        super.onPause()
    }
}