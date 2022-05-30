package com.gmail.dev.wasacz.rpgsoundboard.ui

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DummyFragment : Fragment() {
    override fun onStart() {
        super.onStart()
        val action = DummyFragmentDirections.navigationToHome()
        findNavController().navigate(action)
    }
}