package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Paint
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.annotation.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnStart
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.Transition
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

abstract class FullscreenDialogFragment : DialogFragment(), ICustomTransitionFragment {
    fun finish() {
        findNavController().navigateUp()
        showNavView()
    }
}

interface INavigationActivity {
    fun getCurrentFragment(): Fragment?
}

//#region FAB
interface IFABActivity {
    fun getFAB(): FloatingActionButton?
    fun setupFAB(@DrawableRes drawableRes: Int, @StringRes descriptionRes: Int, listener: View.OnClickListener)
    fun showFAB()
    fun hideFAB()
}

fun Fragment.setupFAB(@DrawableRes drawableRes: Int, @StringRes descriptionRes: Int, listener: View.OnClickListener) {
    activity?.let {
        if (it is IFABActivity) {
            it.setupFAB(drawableRes, descriptionRes, listener)
            it.getFAB()?.slideUp()
        }
    }
}

fun Fragment.showFAB() {
    activity?.let {
        if (it is IFABActivity) {
            it.getFAB()?.slideUp()
            it.showFAB()
        }
    }
}

fun Fragment.hideFAB() {
    activity?.let {
        if (it is IFABActivity)
            it.hideFAB()
    }
}
//#endregion

//#region NavBar
interface INavBarActivity {
    fun getNavMenu(): Menu
    fun getNavView(): NavigationBarView
}

fun Fragment.showNavView() {
    activity?.let {
        if (it is INavBarActivity) {
            it.getNavView().also { view ->
                val dimension = if (view is NavigationRailView) "X" else "Y"
                ObjectAnimator.ofFloat(view, "translation${dimension}", 0f).apply {
                    duration = requireContext().getDuration(com.google.android.material.R.attr.motionDurationMedium1)
                    doOnStart { view.visibility = View.VISIBLE }
                    start()
                }
            }
        }
    }
}

fun Fragment.hideNavView() {
    activity?.let {
        if (it is INavBarActivity) {
            it.getNavView().also { view ->
                view.visibility = View.GONE
                if (view is NavigationRailView) view.translationX = -view.width.toFloat()
                else view.translationY = view.height.toFloat()
            }
        }
    }
}
//#endregion

//#region Views
fun View.show() {
    if (!isShown) {
        alpha = 0f
        visibility = View.VISIBLE
        val duration = resources.getDefaultAnimTime(AnimTime.SHORT)
        animate()
            .alpha(1f)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.hide(targetVisibility: Int = View.GONE) {
    if (isShown) {
        val duration = resources.getDefaultAnimTime(AnimTime.SHORT)
        animate()
            .alpha(0f)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    this@hide.visibility = targetVisibility
                }
            })
    }
}

/**
 * Slides back up view in [CoordinatorLayout].
 */
fun View.slideUp() {
    ((layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? HideBottomViewOnScrollBehavior)?.slideUp(this)
}

/**
 * Sets foreground color, background color and stroke cap of the refresh indicator
 * @return Whether the style has been applied correctly
 */
fun SwipeRefreshLayout.setStyle(
    @ColorInt arrowColor: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    arrowCap: Paint.Cap = Paint.Cap.SQUARE
): Boolean {
    return try {
        val p = SwipeRefreshLayout::class.java.getDeclaredField("mProgress")
        p.isAccessible = true
        val progress = p.get(this) as CircularProgressDrawable
        progress.strokeCap = arrowCap
        if (arrowColor != null) progress.setColorSchemeColors(arrowColor)
        if (backgroundColor != null) setProgressBackgroundColorSchemeColor(backgroundColor)
        true
    } catch (e: Exception) {
        Log.e("REFLECTION ERROR", "setStrokeCap: $e")
        when (e) {
            is NoSuchFieldException, is SecurityException -> false
            else -> throw e
        }
    }
}

/**
 * Sets up the toolbar with [NavController]. Provide [label] if current destination's label is based on navArg.
 * If [fragment's][fragment] [activity][Fragment.getActivity] is [INavBarActivity] it will use [navigation with transitions][navigateUp] for up navigation.
 * **Note** that in that case [fragment's][fragment] [activity][Fragment.getActivity] must use [TransitionViewModel].
 * @see TransitionViewModel
 */
fun MaterialToolbar.setupDefault(fragment: Fragment, label: String? = null) {
    with(fragment) {
        val activity = activity
        val navController = findNavController()
        if (activity is INavBarActivity) {
            setNavigationOnClickListener { navigateUp() }
            val topLevelDestinations = AppBarConfiguration(activity.getNavMenu()).topLevelDestinations
            val isTopLevel = navController.currentDestination?.matchDestinations(topLevelDestinations) ?: true
            if (!isTopLevel) setNavigationIcon(R.drawable.ic_arrow_back_24dp) else navigationIcon = null
            title = label ?: navController.currentDestination?.label
        } else setupWithNavController(navController)
    }
}

/**
 * Sets up the toolbar layout with [NavController]. Provide [label] if current destination's label is based on navArg.
 * **Note** that if [fragment's][fragment] [activity][Fragment.getActivity] is [INavBarActivity], it must use [TransitionViewModel]
 * _([toolbar] is setup with [MaterialToolbar.setupDefault])_.
 * @see MaterialToolbar.setupDefault
 * @see TransitionViewModel
 */
fun CollapsingToolbarLayout.setupDefault(
    toolbar: MaterialToolbar,
    fragment: Fragment,
    label: String? = null
) {
    setContentScrimColor(SurfaceColors.SURFACE_2.getColor(context))
    with(fragment) {
        if (activity is INavBarActivity) {
            toolbar.setupDefault(this)
            title = label ?: findNavController().currentDestination?.label
        } else setupWithNavController(toolbar, findNavController())
    }
}

private fun NavDestination.matchDestinations(destinationIds: Set<Int?>): Boolean = hierarchy.any { destinationIds.contains(it.id) }
//#endregion

//#region Resources
fun Context.getDuration(@AttrRes id: Int): Long {
    val a: TypedArray = obtainStyledAttributes(intArrayOf(id))
    val duration = a.getInt(0, 2000)
    a.recycle()
    return duration.toLong()
}

fun Resources.getSummedDimensionPixelOffset(@DimenRes vararg ids: Int): Int {
    var result = 0
    for (id in ids)
        result += getDimensionPixelOffset(id)
    return result
}

enum class AnimTime {
    SHORT,
    MEDIUM,
    LONG
}

fun Resources.getDefaultAnimTime(animTime: AnimTime): Int = getInteger(
    when (animTime) {
        AnimTime.SHORT -> android.R.integer.config_shortAnimTime
        AnimTime.MEDIUM -> android.R.integer.config_mediumAnimTime
        AnimTime.LONG -> android.R.integer.config_longAnimTime
    }
)

fun Resources.getDefaultAnimTimeLong(animTime: AnimTime): Long = getDefaultAnimTime(animTime).toLong()
//#endregion

//#region Navigation
fun <T> Fragment.setNavigationResult(@StringRes keyId: Int, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(getString(keyId), value)
}

fun <T> Fragment.getNavigationResult(@IdRes id: Int, @StringRes keyId: Int, onResult: (result: T) -> Unit) {
    val key = getString(keyId)
    val navBackStackEntry = findNavController().getBackStackEntry(id)

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME && navBackStackEntry.savedStateHandle.contains(key)) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let(onResult)
            navBackStackEntry.savedStateHandle.remove<T>(key)
        }
    }
    navBackStackEntry.lifecycle.addObserver(observer)

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY)
            navBackStackEntry.lifecycle.removeObserver(observer)
    })
}

interface IToolbarFragment {
    fun getToolbar(): MaterialToolbar
    fun getToolbarLayout(): CollapsingToolbarLayout? = null
}

/**
 * Navigate to sibling destination (eg. from BottomNav).
 * If fragment is specified, transitions will be applied and transition back stack will be switched to this new destination.
 *
 * **Note**: [Fragment's][fragment] [activity][Fragment.getActivity] must use [TransitionViewModel].
 * @see TransitionViewModel
 */
fun NavController.navigate(
    @IdRes id: Int,
    fragment: Fragment?,
    navOptions: NavOptions? = null
) {
    fragment?.run {
        val viewModel by activityViewModels<TransitionViewModel>()
        viewModel.switchBackStack(id)
        TransitionViewModel.applySwitchingTransitions(this)
    }
    navigate(id, null, navOptions)
}

/**
 * Navigate to new destination using provided [action] with [transitions][transitionsBuilder] applied.
 * Use this instead of [NavController.navigate].
 *
 * **Note**: [Fragment's activity][Fragment.getActivity] must use [TransitionViewModel].
 * @see TransitionViewModel
 */
fun Fragment.navigate(action: NavDirections, extras: Navigator.Extras? = null, transitionsBuilder: NavTransitions.() -> Unit) {
    val viewModel by activityViewModels<TransitionViewModel>()
    viewModel.pushBackStack(navTransitions(transitionsBuilder))
    with(findNavController()) {
        if (extras != null) navigate(action, extras)
        else navigate(action)
    }
}

/**
 * Navigate up the navigation hierarchy with transitions applied.
 * Use this instead of [NavController.navigateUp].
 *
 * **Note**: [Fragment's activity][Fragment.getActivity] must use [TransitionViewModel].
 * @see TransitionViewModel
 */
fun Fragment.navigateUp(): Boolean {
    val transitionViewModel by activityViewModels<TransitionViewModel>()
    findNavController().let {
        if (this is ICustomTransitionFragment && !useTransitionViewModel)
            return it.navigateUp()
        val current = it.currentDestination?.id
        val prev = it.previousBackStackEntry?.destination?.id
        val result = it.navigateUp()
        if (result && current != null && prev != null) {
            if (transitionViewModel.isTopLevel(current, prev))
                transitionViewModel.switchBackStack(prev)
            else transitionViewModel.popBackStack()
        }
        return result
    }
}

/**
 * Navigate using provided [action] without any animation.
 * Should be used to navigate to the same fragment with different arguments.
 *
 * **Note**: [Fragment's activity][Fragment.getActivity] must use [TransitionViewModel].
 * @see TransitionViewModel
 */
fun Fragment.refresh(action: NavDirections, navOptions: NavOptions? = null) {
    val transitionViewModel by activityViewModels<TransitionViewModel>()
    transitionViewModel.refresh()
    applyExitTransitions(navTransitions {
        exit = MaterialFade()
    })
    findNavController().navigate(action, navOptions)
}

//#region Transitions
class TransitionViewModel(appBarConfiguration: AppBarConfiguration, @IdRes private val startDestination: Int) : ViewModel() {
    private val topLevelDestinations = appBarConfiguration.topLevelDestinations
    private val backStacks: Map<Int, Stack<NavTransitions>> = topLevelDestinations.associateWith { Stack() }
    private var currentBackStack: Int = topLevelDestinations.run {
        if (contains(startDestination)) startDestination else first()
    }
    private var isSwitchingBackStacks = false
    private var isSwitchFirstDestination = true
    private var isRefreshing = false
    val enterTransitions: NavTransitions?
        get() {
            return when {
                isRefreshing -> {
                    isRefreshing = false
                    navTransitions {}
                }
                isSwitchFirstDestination -> {
                    if (isSwitchFirstDestination) {
                        viewModelScope.launch {
                            delay(10)
                            isSwitchingBackStacks = false
                        }
                    }
                    isSwitchFirstDestination = false
                    navTransitions {
                        enter = MaterialFadeThrough()
                    }
                }
                else -> {
                    try {
                        backStacks[currentBackStack]?.peek()
                    } catch (e: EmptyStackException) {
                        null
                    }
                }
            }
        }

    fun pushBackStack(navTransitions: NavTransitions) {
        backStacks[currentBackStack]?.push(navTransitions) ?: throw NoSuchElementException()
    }

    fun popBackStack() {
        backStacks[currentBackStack]?.pop() ?: throw NoSuchElementException()
    }

    fun switchBackStack(@IdRes id: Int) {
        val newId = if (id == DUMMY_DESTINATION_ID) startDestination else id
        if (!topLevelDestinations.contains(newId)) throw NoSuchElementException()
        currentBackStack = newId
        isSwitchingBackStacks = true
        isSwitchFirstDestination = true
    }

    fun refresh() {
        isRefreshing = true
    }

    fun isTopLevel(@IdRes vararg ids: Int) = ids.all { topLevelDestinations.contains(it) || it == DUMMY_DESTINATION_ID }

    companion object {
        const val DUMMY_DESTINATION_ID = R.id.navigation_dummy

        fun applySwitchingTransitions(fragment: Fragment) {
            fragment.applyExitTransitions(navTransitions {
                exit = MaterialFadeThrough()
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val appBarConfiguration: AppBarConfiguration,
        @IdRes private val startDestination: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TransitionViewModel(appBarConfiguration, startDestination) as T
    }
}

interface ICustomTransitionFragment {
    val useTransitionViewModel: Boolean
        get() = false
}

class NavTransitions {
    var enter: Transition? = null
    var reenter: Transition? = null
    var exit: Transition? = null
    var returnT: Transition? = null
    var sharedEnter: Transition? = null
    var sharedReturn: Transition? = null
}

fun navTransitions(transitionsBuilder: NavTransitions.() -> Unit): NavTransitions = NavTransitions().apply(transitionsBuilder)

fun Fragment.applyTransitions(applyDefault: () -> Unit) {
    try {
        val transitionViewModel by activityViewModels<TransitionViewModel>()
        transitionViewModel.enterTransitions?.let {
            applyTransitions(it)
        } ?: applyDefault()
    } catch (e: RuntimeException) {
        applyDefault()
    }
}

private fun Fragment.applyTransitions(navTransitions: NavTransitions) {
    with(navTransitions) {
        enterTransition = enter
        returnTransition = returnT
        sharedElementEnterTransition = sharedEnter
        sharedElementReturnTransition = sharedReturn

        reenterTransition = reenter
        exitTransition = exit
    }
}

private fun Fragment.applyExitTransitions(navTransitions: NavTransitions) {
    with(navTransitions) {
        reenterTransition = reenter
        exitTransition = exit
    }
}
//#endregion
//#endregion

/**
 * If the set contains the [element], it is removed from the set; otherwise it is added to the set.
 */
fun <E> MutableSet<E>.toggle(element: E) {
    if (contains(element)) remove(element)
    else add(element)
}