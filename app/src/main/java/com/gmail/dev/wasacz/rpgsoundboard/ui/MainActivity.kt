package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.gmail.dev.wasacz.rpgsoundboard.services.MediaPlayerService
import com.gmail.dev.wasacz.rpgsoundboard.ui.library.LibraryViewModel
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val vM by viewModels<LibraryViewModel>()
        viewModel = vM
        setContentView(binding.root)

        val navController = binding.navHostFragment.getFragment<NavHostFragment>().navController
        binding.navView.apply {
            menu.findItem(R.id.menu_placeholder).isEnabled = false
            setupWithNavController(navController)
            val itemSelectedListener = { menuItem: MenuItem ->
                lifecycleScope.launch {
                    binding.mainFab.hide()
                    delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                    navController.navigate(menuItem.itemId)
                }
            }
            setOnItemSelectedListener { menuItem ->
                itemSelectedListener(menuItem)
                true
            }
            setOnItemReselectedListener { menuItem ->
                if(navController.currentDestination?.id != menuItem.itemId) itemSelectedListener(menuItem)
                else navController.navigate(menuItem.itemId)
            }
        }
        navController.addOnDestinationChangedListener { _, destination, args ->
            showOnNavigation(binding.navView, binding.mainFab)
            binding.mainFab.apply {
                when (destination.id) {
                    R.id.navigation_library -> {
                        setImageResource(R.drawable.ic_add_24dp)
                        setOnClickListener {
                            viewModel.saveSongs(context)
                        }
                    }
                    else -> setOnClickListener {}
                }
            }
            lifecycleScope.launch {
                delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                val showFab = args?.getBoolean(resources.getString(R.string.nav_arg_show_fab)) ?: false
                if (showFab) binding.mainFab.show()
            }
        }
    }

    override fun onBackPressed() {
        binding.navHostFragment.getFragment<NavHostFragment>().navController.navigateUp()
    }

    private fun stopService() {
        try {
            stopService(Intent(applicationContext, MediaPlayerService::class.java))
        } catch (e: IllegalStateException) {
            Log.e("SERVICE", "stopService: ${e.cause}\n\n${e.message}")
        }
    }

    private fun showOnNavigation(vararg views: View) {
        for (view in views)
            ((view.layoutParams as CoordinatorLayout.LayoutParams).behavior as HideBottomViewOnScrollBehavior).slideUp(view)
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }
}