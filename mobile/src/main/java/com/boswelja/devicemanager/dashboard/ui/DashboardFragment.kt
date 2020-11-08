package com.boswelja.devicemanager.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentDashboardBinding
import com.boswelja.devicemanager.watchmanager.SelectedWatchHandler
import com.boswelja.devicemanager.watchmanager.WatchStatus

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
            val statusStringRes = when (status) {
                WatchStatus.UNKNOWN -> R.string.watch_status_unknown
                WatchStatus.MISSING_APP -> R.string.watch_status_missing_app
                WatchStatus.NOT_REGISTERED -> R.string.watch_status_not_registered
                WatchStatus.DISCONNECTED -> R.string.watch_status_disconnected
                WatchStatus.CONNECTED -> R.string.watch_status_connected
                else -> R.string.watch_status_error
            }
            binding.watchStatusText.setText(statusStringRes)
        }
    }
}
