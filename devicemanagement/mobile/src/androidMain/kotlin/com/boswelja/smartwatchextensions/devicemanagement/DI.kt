package com.boswelja.smartwatchextensions.devicemanagement

import android.content.Context
import com.boswelja.smartwatchextensions.devicemanagement.database.RegisteredWatchDatabaseLoader
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val deviceManagementModule = module {
    single {
        RegisteredWatchDatabaseLoader(get()).createDatabase()
    }
    single<SelectedWatchManager> {
        SelectedWatchStoreManager(get<Context>().selectedWatchStateStore, get())
    }
    single<WatchRepository> {
        WatchDbRepository(get(), get(), Dispatchers.IO)
    }
}
