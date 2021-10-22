package com.boswelja.smartwatchextensions.settings

import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabaseLoader
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val settingsModule = module {
    single<WatchSettingsRepository> { WatchSettingsDbRepository(get(), Dispatchers.IO) }
    single {
        WatchSettingsDatabaseLoader(get()).createDatabase()
    }
}
