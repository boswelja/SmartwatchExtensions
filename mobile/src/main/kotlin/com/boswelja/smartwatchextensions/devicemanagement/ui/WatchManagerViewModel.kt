package com.boswelja.smartwatchextensions.devicemanagement.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager

class WatchManagerViewModel internal constructor(
    application: Application,
    watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(application, WatchManager.getInstance(application))

    val registeredWatches = watchManager.registeredWatches
}
