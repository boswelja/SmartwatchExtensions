package com.boswelja.devicemanager.dashboard.ui.items

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.dashboard.ui.DashboardFragmentDirections
import com.boswelja.devicemanager.watchmanager.SelectedWatchHandler

class AppManagerDashboardFragment : BaseDashboardItemFragment() {

    private val selectedWatchHandler by lazy { SelectedWatchHandler.get(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWidget(
            getString(R.string.main_app_manager_title)
        ) {
            selectedWatchHandler.selectedWatch.value?.let {
                findNavController().navigate(
                    DashboardFragmentDirections.toAppManagerActivity(
                        it.id, it.name
                    )
                )
            }
        }
    }
}
