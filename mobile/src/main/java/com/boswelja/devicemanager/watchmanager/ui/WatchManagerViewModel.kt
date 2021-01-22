package com.boswelja.devicemanager.watchmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.watchmanager.WatchManager

class WatchManagerViewModel internal constructor(
    application: Application,
    manager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(application, WatchManager.get(application))

    val registeredWatches = manager.registeredWatches
}
