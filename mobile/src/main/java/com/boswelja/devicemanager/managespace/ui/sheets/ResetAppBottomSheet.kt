package com.boswelja.devicemanager.managespace.ui.sheets

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet
import com.boswelja.devicemanager.managespace.ui.ManageSpaceViewModel

/**
 * A [BaseResetBottomSheet] to handle resetting all of Wearable Extensions.
 */
class ResetAppBottomSheet : BaseResetBottomSheet() {

    private val viewModel: ManageSpaceViewModel by activityViewModels()

    private var successful = false

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment {
        return ConfirmationFragment(
            getString(R.string.reset_app_title),
            getString(R.string.reset_app_warning),
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
            getString(R.string.reset_app_restarting_note),
            getString(R.string.button_finish),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!
        ) {
            if (successful) activityManager.clearApplicationUserData()
            dismissSheet()
        }
    }

    override fun onStartWork() {
        viewModel.resetApp(
            { setProgress(it) },
            {
                successful = it
                setProgress(100)
            }
        )
    }
}
