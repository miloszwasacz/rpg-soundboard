package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.gmail.dev.wasacz.rpgsoundboard.services.MediaPlayerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), IFABActivity, IBottomNavActivity {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val vM by viewModels<DatabaseViewModel>()
        viewModel = vM
        setContentView(binding.root)

        val navController = binding.navHostFragment.getFragment<NavHostFragment>().navController
        binding.navView.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { menuItem ->
                navController.navigate(menuItem.itemId, null, navOptions {
                    navController.currentDestination?.id?.let {
                        popUpTo(it)
                    }
                })
                true
            }
        }
        navController.addOnDestinationChangedListener { _, _, args ->
            lifecycleScope.launch {
                delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                val showFab = args?.getBoolean(resources.getString(R.string.nav_arg_show_fab)) ?: false
                if (showFab) binding.mainFab.show() else binding.mainFab.hide()
            }
        }
    }

    override fun setupFAB(@DrawableRes drawableRes: Int, listener: View.OnClickListener) {
        binding.mainFab.apply {
            setImageResource(drawableRes)
            setOnClickListener(listener)
        }
    }

    override fun showFAB() {
        binding.mainFab.show()
    }

    override fun hideFAB() {
        binding.mainFab.hide()
    }

    override fun getBottomMenu(): Menu = binding.navView.menu

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

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }
}