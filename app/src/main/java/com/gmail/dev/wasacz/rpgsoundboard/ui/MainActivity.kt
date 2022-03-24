package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        val navController = binding.navHostFragment.getFragment<NavHostFragment>().navController
        binding.navView.apply {
            menu.findItem(R.id.menu_placeholder).isEnabled = false
            setupWithNavController(navController)
            setOnItemSelectedListener { menuItem ->
                lifecycleScope.launch {
                    binding.mainFab.hide()
                    delay(100)
                    navController.navigate(menuItem.itemId)
                }
                true
            }
        }
        navController.addOnDestinationChangedListener { _, destination, args ->
            binding.mainFab.apply {
                when (destination.id) {
                    R.id.navigation_dashboard -> {
                        setImageResource(R.drawable.ic_dashboard_black_24dp)
                        setOnClickListener {
                            Snackbar.make(binding.mainFab, "Test", Snackbar.LENGTH_SHORT).setAnchorView(binding.mainFab).show()
                        }
                    }
                    R.id.navigation_test -> {
                        setImageResource(R.drawable.ic_home_black_24dp)
                        setOnClickListener {}
                    }
                    else -> setOnClickListener {}
                }
            }
            lifecycleScope.launch {
                delay(100)
                val showFab = args?.getBoolean(resources.getString(R.string.nav_arg_show_fab), false) ?: false
                if (showFab) binding.mainFab.show()
            }
        }
    }
}