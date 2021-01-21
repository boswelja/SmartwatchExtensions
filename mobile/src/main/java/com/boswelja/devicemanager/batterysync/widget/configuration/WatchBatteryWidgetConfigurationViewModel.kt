package com.boswelja.devicemanager.batterysync.widget.configuration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchBatteryWidgetConfigurationViewModel internal constructor(
    application: Application,
    watchDatabase: WatchDatabase
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(application, WatchDatabase.get(application))

    val allRegisteredWatches = watchDatabase.watchDao().getAllObservable()

    fun getWatchByIndex(index: Int): Watch? {
        return allRegisteredWatches.value?.get(index)
    }
}
