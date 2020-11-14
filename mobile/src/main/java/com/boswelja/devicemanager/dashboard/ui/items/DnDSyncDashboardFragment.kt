package com.boswelja.devicemanager.dashboard.ui.items

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.dashboard.ui.DashboardFragmentDirections
import com.boswelja.devicemanager.dndsync.ui.DnDSyncPreferenceWidgetFragment

class DnDSyncDashboardFragment : BaseDashboardItemFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWidget(
            getString(R.string.dashboard_settings_label, getString(R.string.main_dnd_sync_title)),
            DnDSyncPreferenceWidgetFragment()
        ) {
            findNavController().navigate(DashboardFragmentDirections.toDndSyncActivity())
        }
    }
}
