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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.ActivityMainBinding
import com.gmail.dev.wasacz.rpgsoundboard.services.MediaPlayerService
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.ContextMenuFragment
import com.gmail.dev.wasacz.rpgsoundboard.viewmodel.DatabaseViewModel
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), IFABActivity, INavBarActivity, INavigationActivity {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DatabaseViewModel
    private lateinit var transitionViewModel: TransitionViewModel
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val vM by viewModels<DatabaseViewModel>()
        viewModel = vM
        setContentView(binding.root)

        navHost = binding.navHostFragment.getFragment()
        val navController = navHost.navController
        val tVM by viewModels<TransitionViewModel> { TransitionViewModel.Factory(AppBarConfiguration(getNavMenu()), R.id.navigation_home) }
        transitionViewModel = tVM
        binding.navView.apply {
            setupWithNavController(navController)
            val navigate = { id: Int, navOptions: NavOptions ->
                navController.navigate(id, getCurrentFragment(), navOptions)
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
    override fun getFAB(): FloatingActionButton = binding.mainFab

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

    //#region INavigationActivity
    override fun getCurrentFragment(): Fragment? = navHost.childFragmentManager.primaryNavigationFragment
    //#endregion

    override fun onBackPressed() {
        getCurrentFragment()?.let {
            if (it is FullscreenDialogFragment) it.showNavView()
            it.navigateUp()
        } ?: navHost.navController.navigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        getCurrentFragment()?.let {
            if (it is FullscreenDialogFragment) it.showNavView()
            return it.navigateUp()
        }
        return super.onSupportNavigateUp()
    }

    private fun stopService() {
        try {
            stopService(Intent(applicationContext, MediaPlayerService::class.java))
        } catch (e: IllegalStateException) {
            Log.e("SERVICE", "stopService: ${e.cause}\n\n${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }
}