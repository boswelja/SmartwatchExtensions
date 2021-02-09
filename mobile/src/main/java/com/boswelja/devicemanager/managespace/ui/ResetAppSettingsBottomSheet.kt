package com.boswelja.devicemanager.managespace.ui

import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.R

/**
 * A [BaseResetBottomSheet] to handle resetting all app settings. This does not include extension
 * settings.
 */
class ResetAppSettingsBottomSheet : BaseResetBottomSheet() {

    private var successful = false

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment {
        return ConfirmationFragment(
            getString(R.string.reset_settings_title),
            getString(R.string.reset_settings_desc),
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
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!
        ) {
            dismissSheet()
            activity?.recreate()
        }
    }

    override fun onStartWork() {
        viewModel.resetAppSettings(
            { setProgress(it) },
            {
                successful = it
                setProgress(100)
            }
        )
    }
}
