package com.gmail.dev.wasacz.rpgsoundboard.ui.generic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import androidx.viewbinding.ViewBinding
import com.google.android.material.transition.MaterialFadeThrough

typealias DataBindingInflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class DataBindingFragment<T : ViewBinding>(
    private val inflate: DataBindingInflate<T>,
    private val enterTransition: Transition? = MaterialFadeThrough(),
    private val exitTransition: Transition? = MaterialFadeThrough()
) : Fragment() {
    /**
     * This property is only valid between onCreateView and onDestroyView.
     */
    protected val binding: T get() = _binding!!
    private var _binding: T? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEnterTransition(enterTransition)
        setExitTransition(exitTransition)
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}