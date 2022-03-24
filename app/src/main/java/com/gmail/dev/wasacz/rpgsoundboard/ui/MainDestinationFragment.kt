package com.gmail.dev.wasacz.rpgsoundboard.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.google.android.material.transition.MaterialFadeThrough

abstract class MainDestinationFragment<B : ViewBinding>(inflate: DataBindingInflate<B>) : DataBindingFragment<B>(inflate) {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }
}