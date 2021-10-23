package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val batterySyncModule = module {
    single {
        BatteryStatsDatabase(
            get {
                parametersOf(BatteryStatsDatabase.Schema, "batterystats.db")
            }
        )
    }
    single<BatteryStatsRepository> { BatteryStatsDbRepository(get(), get(named("database"))) }
}
