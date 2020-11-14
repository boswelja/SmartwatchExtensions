package com.boswelja.devicemanager.dashboard.ui.items

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.ui.BatterySyncPreferenceWidgetFragment
import com.boswelja.devicemanager.dashboard.ui.DashboardFragmentDirections

class BatterySyncDashboardFragment : BaseDashboardItemFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWidget(
            getString(
                R.string.dashboard_settings_label,
                getString(R.string.main_battery_sync_title)
            ),
            BatterySyncPreferenceWidgetFragment()
        ) {
            findNavController().navigate(DashboardFragmentDirections.toBatterySyncActivity())
        }
    }
}
