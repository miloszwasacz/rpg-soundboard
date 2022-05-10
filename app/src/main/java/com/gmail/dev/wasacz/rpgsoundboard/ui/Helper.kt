package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.elevation.SurfaceColors

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

fun CollapsingToolbarLayout.setupDefault(context: Context?, toolbar: MaterialToolbar, navController: NavController) {
    setupWithNavController(toolbar, navController)
    context?.let { setContentScrimColor(SurfaceColors.SURFACE_2.getColor(it)) }
}
//#endregion

//#region Resources
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