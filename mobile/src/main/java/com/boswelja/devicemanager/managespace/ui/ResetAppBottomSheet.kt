package com.boswelja.devicemanager.managespace.ui

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.ContextCompat
import com.boswelja.devicemanager.R

class ResetAppBottomSheet : BaseResetBottomSheet() {

    private var successful = false

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment {
        return ConfirmationFragment(
            getString(R.string.manage_space_reset_app_title),
            getString(R.string.manage_space_reset_app_desc),
            getString(R.string.dialog_button_reset),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)!!,
            showProgress
        )
    }

    override fun onCreateProgress(): ProgressFragment {
        return ProgressFragment(
            getString(R.string.reset_app_resetting),
            getString(R.string.please_wait)
        )
    }

    override fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment {
        val activityManager = requireContext()
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val title = if (successful)
            getString(R.string.reset_app_success)
        else
            getString(R.string.reset_app_failed)

        return ConfirmationFragment(
            title,
            "",
            getString(R.string.button_done),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!
        ) {
            if (successful) activityManager.clearApplicationUserData()
            dismissSheet()
        }
    }

    override fun onStartWork() {
        viewModel.clearCache(
            { setProgress(it) },
            {
                successful = it
                setProgress(100)
            }
        )
    }
}
