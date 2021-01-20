package com.boswelja.devicemanager.batterysync.widget.configuration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WatchBatteryWidgetConfigurationViewModel internal constructor(
    application: Application,
    watchDatabase: WatchDatabase
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(application, WatchDatabase.get(application))

    val allRegisteredWatches = watchDatabase.watchDao().getAllObservable()

    fun addWidgetToDatabase(watchBatteryWidgetId: WatchBatteryWidgetId): Job {
        return viewModelScope.launch {
            WidgetDatabase.open(getApplication()).also {
                it.watchBatteryWidgetDao().addWidget(watchBatteryWidgetId)
                it.close()
            }
            WatchBatteryWidget.updateWidgets(
                getApplication(),
                intArrayOf(watchBatteryWidgetId.widgetId)
            )
        }
    }
}
