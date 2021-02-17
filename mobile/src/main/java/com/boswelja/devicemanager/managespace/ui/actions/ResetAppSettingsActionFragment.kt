package com.boswelja.devicemanager.managespace.ui.actions

import android.os.Bundle
import android.view.View
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet
import com.boswelja.devicemanager.managespace.ui.sheets.ResetAppSettingsBottomSheet

/**
 * A [ActionFragment] to handle resetting all app settings. This does not include extension
 * settings.
 */
class ResetAppSettingsActionFragment : ActionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            title.text = getString(R.string.reset_settings_title)
            desc.text = getString(R.string.reset_settings_desc)
            button.text = getString(R.string.reset_settings_title)
        }
    }

    override fun onCreateSheet(): BaseResetBottomSheet = ResetAppSettingsBottomSheet()
}
