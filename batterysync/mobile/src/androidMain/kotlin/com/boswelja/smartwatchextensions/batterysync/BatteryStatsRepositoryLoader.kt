package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.database.DatabaseDriverFactory
import kotlinx.coroutines.Dispatchers

object BatteryStatsRepositoryLoader : SingletonHolder<BatteryStatsRepository, Context>(
    {
        val driver = DatabaseDriverFactory(it.applicationContext).createDriver()
        val database = BatteryStatsDatabase(driver)
        BatteryStatsRepository(database, Dispatchers.IO)
    }
)
