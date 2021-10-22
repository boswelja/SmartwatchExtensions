package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabaseLoader
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val appManagerModule = module {
    single { WatchAppDatabaseLoader(get()).createDatabase() }
    single<WatchAppRepository> { WatchAppDbRepository(get(), Dispatchers.IO) }
}
