package com.boswelja.smartwatchextensions.devicemanagement.ui

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager

class WatchManagerViewModel(
    watchManager: WatchManager
) : ViewModel() {
    val registeredWatches = watchManager.registeredWatches
}
