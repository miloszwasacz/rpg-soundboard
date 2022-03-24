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
import com.gmail.dev.wasacz.rpgsoundboard.ui.MainDestinationFragment

class HomeFragment : MainDestinationFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
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
        findNavController().navigate(R.id.navigation_home_to_test)
    }
}