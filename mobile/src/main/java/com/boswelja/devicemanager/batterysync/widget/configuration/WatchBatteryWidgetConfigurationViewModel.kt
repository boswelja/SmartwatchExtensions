package com.boswelja.devicemanager.batterysync.widget.configuration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase

class WatchBatteryWidgetConfigurationViewModel internal constructor(
    application: Application,
    watchDatabase: WatchDatabase
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(application, WatchDatabase.get(application))

    val allRegisteredWatches = watchDatabase.watchDao().getAllObservable()
}
