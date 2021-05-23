package com.boswelja.smartwatchextensions.batterysync.widget.config

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.smartwatchextensions.watchmanager.database.WatchDatabase

class BatteryWidgetConfigViewModel internal constructor(
    application: Application,
    watchDatabase: WatchDatabase
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchDatabase.getInstance(application)
    )

    val registeredWatches = watchDatabase.watchDao().getAll()
}
