package com.boswelja.devicemanager.watchinfo.ui

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet

/**
 * A [BaseResetBottomSheet] for clearing prefrences for a single watch.
 */
class ClearWatchPreferencesSheet : BaseResetBottomSheet() {

    private val viewModel: WatchInfoViewModel by activityViewModels()

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment =
        ConfirmationFragment(
            getString(R.string.clear_preferences_dialog_title),
            getString(R.string.clear_preferences_dialog_message, viewModel.watch.value?.name),
            getString(R.string.button_forget_watch),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)!!,
            showProgress
        )

    override fun onCreateProgress(): ProgressFragment =
        ProgressFragment(
            getString(R.string.clear_preferences_clearing),
            getString(R.string.please_wait)
        )

    override fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment =
        ConfirmationFragment(
            getString(R.string.clear_preferences_success),
            "",
            getString(R.string.button_done),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!,
            dismissSheet
        )

    override fun onStartWork() {
        viewModel.resetWatchPreferences()
        setProgress(100)
    }
}
