package com.gmail.dev.wasacz.rpgsoundboard.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gmail.dev.wasacz.rpgsoundboard.R
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentHomeBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.applyTransitions
import com.gmail.dev.wasacz.rpgsoundboard.ui.generic.DataBindingFragment
import com.google.android.material.transition.MaterialFadeThrough

class HomeFragment : DataBindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTransitions {
            enterTransition = MaterialFadeThrough()
            exitTransition = MaterialFadeThrough()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val homeViewModel by viewModels<HomeViewModel>()

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        binding.button.setOnClickListener {
            navigate()
        }
        return binding.root
    }

    fun navigate() {
        findNavController().navigate(R.id.navigation_home_to_player)
    }
}