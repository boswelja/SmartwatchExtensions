package com.boswelja.devicemanager.ui.batterysync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStatsDatabase

class BatteryStatsModel(application: Application) : AndroidViewModel(application) {
    private val database: WatchBatteryStatsDatabase = WatchBatteryStatsDatabase.open(application)

    fun getBatteryStatsObservable(watchId: String): LiveData<WatchBatteryStats?> {
        return database.batteryStatsDao().getObservableStatsForWatch(watchId)
    }
}