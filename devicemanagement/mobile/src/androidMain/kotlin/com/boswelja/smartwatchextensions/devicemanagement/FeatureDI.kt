package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatchDatabase
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * A Koin module for providing device management repositories.
 */
val deviceManagementModule = module {
    single {
        RegisteredWatchDatabase(
            get {
                parametersOf(RegisteredWatchDatabase.Schema, "registeredwatches.db")
            }
        )
    }
    single<SelectedWatchManager> {
        SelectedWatchStoreManager(get<Context>().selectedWatchStateStore, get())
    }
    single<WatchRepository> {
        WatchDbRepository(get(), get(), get(named("database")))
    }
}
