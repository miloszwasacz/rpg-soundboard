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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.gmail.dev.wasacz.rpgsoundboard.services.MediaPlayerService
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ContextMenuFragment
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), IFABActivity, INavBarActivity {
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
            val navigate = { id: Int, navOptions: NavOptions ->
                with(navController) {
                    navHost.childFragmentManager.primaryNavigationFragment.let {
                        if (it !== null && it is IToolbarFragment)
                            navigate(id, it.getToolbar(), it.getToolbarLayout(), navOptions)
                        else navigate(id, null, navOptions)
                    }
                }
            }
            setOnItemSelectedListener { menuItem ->
                val navOptions = navOptions {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
                navigate(menuItem.itemId, navOptions)
                true
            }
            setOnItemReselectedListener { menuItem ->
                val navOptions = navOptions {
                    launchSingleTop = true
                    popUpTo(navController.graph.findStartDestination().id)
                }
                navigate(menuItem.itemId, navOptions)
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

    //#region IBottomNavActivity
    override fun getNavMenu(): Menu = binding.navView.menu
    override fun getNavView(): NavigationBarView = binding.navView
    //#endregion

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