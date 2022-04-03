package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.library.LibraryViewModel
import com.google.android.material.snackbar.Snackbar
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
            setOnItemSelectedListener { menuItem ->
                lifecycleScope.launch {
                    binding.mainFab.hide()
                    delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                    navController.navigate(menuItem.itemId)
                }
                true
            }
        }
        navController.addOnDestinationChangedListener { _, destination, args ->
            binding.mainFab.apply {
                when (destination.id) {
                    R.id.navigation_library -> {
                        setImageResource(R.drawable.ic_add_24dp)
                        setOnClickListener {
                            Snackbar.make(binding.mainFab, "Test", Snackbar.LENGTH_SHORT).setAnchorView(binding.mainFab).show()
                        }
                    }
                    R.id.navigation_test -> {
                        setImageResource(R.drawable.ic_home_24dp)
                        setOnClickListener {}
                    }
                    else -> setOnClickListener {}
                }
            }
            lifecycleScope.launch {
                delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                val showFab = args?.getBoolean(resources.getString(R.string.nav_arg_show_fab), false) ?: false
                if (showFab) binding.mainFab.show()
            }
        }
    }
}