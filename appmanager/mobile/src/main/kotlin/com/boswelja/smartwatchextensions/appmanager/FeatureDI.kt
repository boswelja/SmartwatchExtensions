package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.smartwatchextensions.appmanager.database.WatchAppDatabase
import com.boswelja.smartwatchextensions.appmanager.database.watchAppDbAdapter
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * A Koin module for providing repositories for App Manager.
 */
internal val appManagerCommonModule = module {
    single {
        WatchAppDatabase(
            get { parametersOf(WatchAppDatabase.Schema, "watchapps.db") },
            watchAppDbAdapter
        )
    }
    single<WatchAppRepository> { WatchAppDbRepository(get(), get(named("database"))) }
}
