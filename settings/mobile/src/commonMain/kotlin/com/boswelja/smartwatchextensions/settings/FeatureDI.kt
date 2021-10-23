package com.boswelja.smartwatchextensions.settings

import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabase
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {
    single<WatchSettingsRepository> { WatchSettingsDbRepository(get(), get(named("database"))) }
    single {
        WatchSettingsDatabase(
            get {
                parametersOf(WatchSettingsDatabase.Schema, "watchsettings.db")
            }
        )
    }
}
