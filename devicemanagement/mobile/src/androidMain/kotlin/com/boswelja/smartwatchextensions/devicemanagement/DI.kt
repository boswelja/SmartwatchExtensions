package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatchDatabase
import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatchDatabaseLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val deviceManagementModule = DI.Module(name = "DeviceManagement") {
    import(deviceManagementCommonModule)
    bind<RegisteredWatchDatabase>() with singleton {
        RegisteredWatchDatabaseLoader(instance()).createDatabase()
    }
    bind<SelectedWatchManager>() with singleton {
        SelectedWatchStoreManager(instance<Context>().selectedWatchStateStore, instance())
    }
}
