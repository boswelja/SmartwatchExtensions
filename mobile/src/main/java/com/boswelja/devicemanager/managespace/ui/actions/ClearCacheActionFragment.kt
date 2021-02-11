package com.boswelja.devicemanager.managespace.ui.actions

import android.os.Bundle
import android.view.View
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.managespace.ui.sheets.BaseResetBottomSheet
import com.boswelja.devicemanager.managespace.ui.sheets.ClearCacheBottomSheet

/**
 * A [ActionFragment] to handle clearing Wearable Extensions cache.
 */
class ClearCacheActionFragment : ActionFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            title.text = getString(R.string.clear_cache_title)
            desc.text = getString(R.string.clear_cache_desc)
            button.text = getString(R.string.clear_cache_title)
        }
    }

    override fun onCreateSheet(): BaseResetBottomSheet = ClearCacheBottomSheet()
}
