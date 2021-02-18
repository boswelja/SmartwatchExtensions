package com.boswelja.devicemanager.managespace.ui.sheets

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet
import com.boswelja.devicemanager.managespace.ui.ManageSpaceViewModel

/**
 * A [BaseResetBottomSheet] to handle clearing Wearable Extensions cache.
 */
class ClearCacheBottomSheet : BaseResetBottomSheet() {

    private val viewModel: ManageSpaceViewModel by activityViewModels()

    private var successful = false

    override fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment {
        return ConfirmationFragment(
            getString(R.string.clear_cache_title),
            getString(R.string.clear_cache_desc),
            getString(R.string.dialog_button_reset),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right)!!,
            showProgress
        )
    }

    override fun onCreateProgress(): ProgressFragment {
        return ProgressFragment(
            getString(R.string.clear_cache_clearing),
            getString(R.string.please_wait)
        )
    }

    override fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment {
        val title = if (successful)
            getString(R.string.clear_cache_success)
        else
            getString(R.string.clear_cache_failed)

        return ConfirmationFragment(
            title,
            "",
            getString(R.string.button_done),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!,
            dismissSheet
        )
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
