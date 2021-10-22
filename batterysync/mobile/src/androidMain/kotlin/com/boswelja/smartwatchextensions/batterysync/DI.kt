package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabaseLoader
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val batterySyncModule = module {
    single { BatteryStatsDatabaseLoader(get()).createDatabase() }
    single<BatteryStatsRepository> { BatteryStatsDbRepository(get(), Dispatchers.IO) }
}
