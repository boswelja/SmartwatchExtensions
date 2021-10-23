package com.boswelja.smartwatchextensions.batterysync.widget.config

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository

class BatteryWidgetConfigViewModel(
    watchRepository: WatchRepository
) : ViewModel() {

    val registeredWatches = watchRepository.registeredWatches
}
