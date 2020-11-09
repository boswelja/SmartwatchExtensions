package com.boswelja.devicemanager.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.databinding.FragmentDashboardBinding
import com.boswelja.devicemanager.watchmanager.SelectedWatchHandler
import timber.log.Timber

class DashboardFragment : Fragment() {

    private val selectedWatchHandler by lazy { SelectedWatchHandler.get(requireContext()) }

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        selectedWatchHandler.status.observe(this) { status ->
            Timber.d("Got watch status: $status")
            binding.watchStatusText.setText(status.stringRes)
            binding.watchStatusIcon.setImageResource(status.iconRes)
        }
    }

    override fun onResume() {
        super.onResume()
        selectedWatchHandler.refreshStatus()
    }
}
