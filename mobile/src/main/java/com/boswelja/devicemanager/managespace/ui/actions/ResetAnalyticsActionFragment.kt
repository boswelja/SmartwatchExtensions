package com.boswelja.devicemanager.managespace.ui.actions

import android.os.Bundle
import android.view.View
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet
import com.boswelja.devicemanager.managespace.ui.sheets.ResetAnalyticsBottomSheet

/**
 * A [ActionFragment] to handle resetting analytics storage.
 */
class ResetAnalyticsActionFragment : ActionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            title.text = getString(R.string.reset_analytics_title)
            desc.text = getString(R.string.reset_analytics_desc)
            button.text = getString(R.string.reset_analytics_title)
        }
    }

    override fun onCreateSheet(): BaseResetBottomSheet = ResetAnalyticsBottomSheet()
}
