package com.boswelja.devicemanager.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.ui.BatterySyncPreferenceWidgetFragment
import com.boswelja.devicemanager.databinding.DashboardItemBinding
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
        setupWidget(
            binding.batterySyncWidget,
            getString(
                R.string.dashboard_settings_label,
                getString(R.string.main_battery_sync_title)
            ),
            BatterySyncPreferenceWidgetFragment()
        ) {
            findNavController().navigate(DashboardFragmentDirections.toBatterySyncActivity())
        }

        // Set up Do not Disturb Sync widget
        setupWidget(
            binding.dndSyncWidget,
            getString(R.string.dashboard_settings_label, getString(R.string.main_dnd_sync_title))
        ) {
            findNavController().navigate(DashboardFragmentDirections.toDndSyncActivity())
        }

        // Set up Phone Locking widget
        setupWidget(
            binding.phoneLockingWidget,
            getString(
                R.string.dashboard_settings_label,
                getString(R.string.main_phone_locking_title)
            )
        ) {
            findNavController().navigate(DashboardFragmentDirections.toPhoneLockingActivity())
        }

        // Set up App Manager widget
        setupWidget(
            binding.appManagerWidget,
            getString(R.string.main_app_manager_title)
        ) {
            selectedWatchHandler.selectedWatch.value?.let {
                findNavController().navigate(
                    DashboardFragmentDirections.toAppManagerActivity(
                        it.id,
                        it.name
                    )
                )
            }
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

    private fun setupWidget(
        binding: DashboardItemBinding,
        actionLabel: String,
        widgetContent: Fragment? = null,
        actionClickListener: () -> Unit
    ) {
        widgetContent?.let {
            childFragmentManager.beginTransaction()
                .replace(binding.itemContent.id, it)
                .commit()
        }
        binding.settingsAction.apply {
            setOnClickListener { actionClickListener() }
            text = actionLabel
        }
    }
}
