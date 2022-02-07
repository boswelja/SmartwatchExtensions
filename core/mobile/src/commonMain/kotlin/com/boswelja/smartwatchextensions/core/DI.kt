package com.boswelja.smartwatchextensions.core

import com.boswelja.smartwatchextensions.core.settings.WatchSettingsDbRepository
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabase
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * A Koin module for providing core dependencies.
 */
val coreModule = module {
    single<WatchSettingsRepository> { WatchSettingsDbRepository(get(), get(named("database"))) }
    single {
        WatchSettingsDatabase(
            get {
                parametersOf(WatchSettingsDatabase.Schema, "watchsettings.db")
            }
        )
    }
}
