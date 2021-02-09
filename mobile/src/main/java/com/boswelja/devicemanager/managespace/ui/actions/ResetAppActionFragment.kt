package com.boswelja.devicemanager.managespace.ui.actions

import android.os.Bundle
import android.view.View
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.managespace.ui.sheets.BaseResetBottomSheet
import com.boswelja.devicemanager.managespace.ui.sheets.ResetAppBottomSheet

/**
 * A [ActionFragment] to handle resetting all of Wearable Extensions.
 */
class ResetAppActionFragment : ActionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            title.text = getString(R.string.reset_app_title)
            desc.text = getString(R.string.reset_app_desc)
            button.text = getString(R.string.reset_app_title)
        }
    }

    override fun onCreateSheet(): BaseResetBottomSheet = ResetAppBottomSheet()
}
