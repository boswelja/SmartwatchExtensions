package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager

/**
 * A ViewModel for providing data to Watch Manager.
 */
class WatchManagerViewModel(
    watchManager: WatchManager
) : ViewModel() {

    /**
     * Flow the list of registered watches.
     */
    val registeredWatches = watchManager.registeredWatches
}
