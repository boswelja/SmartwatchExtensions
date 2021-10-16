package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.smartwatchextensions.appmanager.database.DB_DISPATCHER
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val appManagerCommonModule = DI.Module(name = "AppManagerCommon") {
    bind<WatchAppRepository>() with singleton {
        WatchAppDbRepository(instance(), DB_DISPATCHER)
    }
}
