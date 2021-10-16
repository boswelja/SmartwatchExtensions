package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabaseLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val appManagerModule = DI.Module(name = "AppManager") {
    import(appManagerCommonModule)
    bind<WatchAppDatabase>() with singleton { WatchAppDatabaseLoader(instance()).createDatabase() }
}
