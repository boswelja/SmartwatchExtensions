package com.boswelja.devicemanager.dashboard.ui.items

import android.os.Bundle
import android.view.View
import com.boswelja.devicemanager.R

class AppManagerDashboardFragment : BaseDashboardItemFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWidget(
            getString(R.string.main_app_manager_title)
        ) {
            // findNavController().navigate(DashboardFragmentDirections.toAppManagerActivity())
        }
    }
}
