package com.gmail.dev.wasacz.rpgsoundboard.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.gmail.dev.wasacz.rpgsoundboard.databinding.FragmentDashboardBinding
import com.gmail.dev.wasacz.rpgsoundboard.ui.MainDestinationFragment

class DashboardFragment : MainDestinationFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val dashboardViewModel by viewModels<DashboardViewModel>()

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return binding.root
    }
}