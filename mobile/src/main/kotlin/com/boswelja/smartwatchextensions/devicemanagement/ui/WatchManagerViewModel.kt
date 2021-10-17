package com.boswelja.smartwatchextensions.devicemanagement.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class WatchManagerViewModel(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchManager: WatchManager by instance()

    val registeredWatches = watchManager.registeredWatches
}
