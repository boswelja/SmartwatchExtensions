package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.database.DB_DISPATCHER
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val batterySyncCommonModule = DI.Module(name = "BatterySyncCommon") {
    bind<BatteryStatsRepository>() with singleton {
        BatteryStatsDbRepository(instance(), DB_DISPATCHER)
    }
}
