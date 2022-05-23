package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.gmail.dev.wasacz.rpgsoundboard.services.MediaPlayerService
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ContextMenuFragment
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

        val navHost = binding.navHostFragment.getFragment<NavHostFragment>()
        val navController = navHost.navController
        binding.navView.apply {
            setupWithNavController(navController)
            setOnItemSelectedListener { menuItem ->
                val navOptions = navOptions {
                    navController.currentDestination?.id?.let {
                        popUpTo(it)
                    }
                }
                with(navController) {
                    navHost.childFragmentManager.primaryNavigationFragment.let {
                        if (it !== null && it is IToolbarFragment)
                            navigate(menuItem.itemId, it.getToolbar(), it.getToolbarLayout(), navOptions)
                        else navigate(menuItem.itemId, null, navOptions)
                    }
                }
                true
            }
        }
        navController.addOnDestinationChangedListener { _, _, args ->
            lifecycleScope.launch {
                delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                val showFab = args?.getBoolean(resources.getString(R.string.nav_arg_show_fab)) ?: false
                val isActionModeStarted = navHost.childFragmentManager.fragments.firstOrNull()?.let {
                    (it as? ContextMenuFragment<*, *, *>)?.isActionModeStarted()
                } ?: false
                if (showFab && !isActionModeStarted) binding.mainFab.show() else binding.mainFab.hide()
            }
        }
    }

    //#region IFABActivity
    override fun setupFAB(@DrawableRes drawableRes: Int, @StringRes descriptionRes: Int, listener: View.OnClickListener) {
        lifecycleScope.launchWhenCreated {
            binding.mainFab.apply {
                hide()
                delay(resources.getDefaultAnimTimeLong(AnimTime.SHORT))
                setImageResource(drawableRes)
                contentDescription = resources.getString(descriptionRes)
                setOnClickListener(listener)
            }
        }
    }

    override fun showFAB() {
        binding.mainFab.show()
    }

    override fun hideFAB() {
        binding.mainFab.hide()
    }
    //#endregion

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