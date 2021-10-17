package com.boswelja.smartwatchextensions.batterysync.widget.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchRepository
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class BatteryWidgetConfigViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchRepository: WatchRepository by instance()

    val registeredWatches = watchRepository.registeredWatches
}
