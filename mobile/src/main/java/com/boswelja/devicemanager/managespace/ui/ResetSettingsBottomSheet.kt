package com.boswelja.devicemanager.managespace.ui

import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.R

/**
 * A [BaseResetBottomSheet] to handle resetting all extension-related settings.
 */
class ResetSettingsBottomSheet : BaseResetBottomSheet() {

    private var successful = false

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment {
        return ConfirmationFragment(
            getString(R.string.manage_space_reset_extensions_title),
            getString(R.string.manage_space_reset_extensions_desc),
            getString(R.string.dialog_button_reset),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)!!,
            showProgress
        )
    }

    override fun onCreateProgress(): ProgressFragment {
        return ProgressFragment(
            getString(R.string.reset_settings_resetting),
            getString(R.string.please_wait)
        )
    }

    override fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment {
        val title = if (successful)
            getString(R.string.reset_settings_success)
        else
            getString(R.string.reset_settings_failed)

        return ConfirmationFragment(
            title,
            "",
            getString(R.string.button_done),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!,
            dismissSheet
        )
    }

    override fun onStartWork() {
        viewModel.resetSettings(
            { setProgress(it) },
            {
                successful = it
                setProgress(100)
            }
        )
    }
}
