package com.boswelja.smartwatchextensions.batterysync

import org.koin.dsl.module

val batterySyncModule = module {
    single<BatteryStatsRepository> { BatteryStatsDsRepository(get()) }
    single<BatterySyncStateRepository> { BatterySyncStateDsRepository(get()) }
}
