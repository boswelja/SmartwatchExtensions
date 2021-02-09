package com.boswelja.devicemanager.managespace.ui

import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.R

/**
 * A [BaseResetBottomSheet] to handle resetting analytics storage.
 */
class ResetAnalyticsBottomSheet : BaseResetBottomSheet() {

    private var successful = false

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment {
        return ConfirmationFragment(
            getString(R.string.reset_analytics_title),
            getString(R.string.reset_analytics_desc),
            getString(R.string.dialog_button_reset),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)!!,
            showProgress
        )
    }

    override fun onCreateProgress(): ProgressFragment {
        return ProgressFragment(
            getString(R.string.reset_analytics_resetting),
            getString(R.string.please_wait)
        )
    }

    override fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment {
        val title = if (successful)
            getString(R.string.reset_analytics_success)
        else
            getString(R.string.reset_analytics_failed)

        return ConfirmationFragment(
            title,
            "",
            getString(R.string.button_done),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!,
            dismissSheet
        )
    }

    override fun onStartWork() {
        viewModel.resetAnalytics {
            successful = it
            setProgress(100)
        }
    }
}
