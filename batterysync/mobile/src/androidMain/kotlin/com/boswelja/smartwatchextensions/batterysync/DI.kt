package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabase
import com.boswelja.smartwatchextensions.batterysync.database.BatteryStatsDatabaseLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val batterySyncModule = DI.Module(name = "BatterySync") {
    import(batterySyncCommonModule)
    bind<BatteryStatsDatabase>() with singleton {
        BatteryStatsDatabaseLoader(instance()).createDatabase()
    }
}
