package com.boswelja.devicemanager.watchinfo.ui

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet

/**
 * A [BaseResetBottomSheet] for forgetting a single watch.
 */
class ForgetWatchSheet : BaseResetBottomSheet() {

    private val viewModel: WatchInfoViewModel by activityViewModels()

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment =
        ConfirmationFragment(
            getString(R.string.forget_watch_dialog_title),
            getString(
                R.string.forget_watch_dialog_message,
                viewModel.watch.value?.name,
                viewModel.watch.value?.name
            ),
            getString(R.string.button_forget_watch),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)!!,
            showProgress
        )

    override fun onCreateProgress(): ProgressFragment =
        ProgressFragment(
            getString(R.string.forget_watch_forgetting),
            getString(R.string.please_wait)
        )

    override fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment =
        ConfirmationFragment(
            getString(R.string.forget_watch_success),
            getString(R.string.forget_watch_warning),
            getString(R.string.button_finish),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!
        ) {
            dismissSheet()
            activity?.finish()
        }

    override fun onStartWork() {
        viewModel.forgetWatch()
        setProgress(100)
    }
}
