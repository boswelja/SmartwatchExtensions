package com.boswelja.devicemanager.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.ui.BatterySyncPreferenceWidgetFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Battery Sync widget
        childFragmentManager.beginTransaction()
            .replace(
                binding.batterySyncWidget.itemContent.id,
                BatterySyncPreferenceWidgetFragment()
            ).commit()
        binding.batterySyncWidget.settingsAction.apply {
            setOnClickListener {
                findNavController().navigate(DashboardFragmentDirections.toBatterySyncActivity())
            }
            text = getString(
                R.string.dashboard_settings_label,
                getString(R.string.main_battery_sync_title)
            )
        }

        // Set up Do not Disturb Sync widget
        binding.dndSyncWidget.settingsAction.apply {
            setOnClickListener {
                findNavController().navigate(DashboardFragmentDirections.toDndSyncActivity())
            }
            text = getString(
                R.string.dashboard_settings_label,
                getString(R.string.main_dnd_sync_title)
            )
        }

        // Set up Phone Locking widget
        binding.phoneLockingWidget.settingsAction.apply {
            setOnClickListener {
                findNavController().navigate(DashboardFragmentDirections.toPhoneLockingActivity())
            }
            text = getString(
                R.string.dashboard_settings_label,
                getString(R.string.main_phone_locking_title)
            )
        }

        // Set up App Manager widget
        binding.appManagerWidget.settingsAction.apply {
            setOnClickListener {
                selectedWatchHandler.selectedWatch.value?.let {
                    findNavController().navigate(
                        DashboardFragmentDirections.toAppManagerActivity(
                            it.id,
                            it.name
                        )
                    )
                }
            }
            text = getString(R.string.main_app_manager_title)
        }
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
