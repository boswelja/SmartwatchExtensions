package com.boswelja.smartwatchextensions.batterysync.widget.config

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository

/**
 * A ViewModel to provide data for [BatteryWidgetConfigActivity].
 */
class BatteryWidgetConfigViewModel(
    watchRepository: WatchRepository
) : ViewModel() {

    /**
     * Flow a list of registered watches.
     */
    val registeredWatches = watchRepository.registeredWatches
}
