package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.smartwatchextensions.devicemanagement.database.DB_DISPATCHER
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val deviceManagementCommonModule = DI.Module(name = "DeviceManagementCommon") {
    bind<WatchRepository>() with singleton {
        WatchDbRepository(instance(), instance(), DB_DISPATCHER)
    }
}
