package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.database.DatabaseDriverFactory
import kotlinx.coroutines.Dispatchers

object BatteryStatsDbRepositoryLoader : SingletonHolder<BatteryStatsDbRepository, Context>(
    {
        val driver = DatabaseDriverFactory(it.applicationContext).createDriver()
        val database = BatteryStatsDatabase(driver)
        BatteryStatsDbRepository(database, Dispatchers.IO)
    }
)
