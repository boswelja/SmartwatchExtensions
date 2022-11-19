package com.boswelja.smartwatchextensions.core

import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchDbRepository
import com.boswelja.smartwatchextensions.core.watches.registered.RegisteredWatchRepository
import com.boswelja.smartwatchextensions.core.devicemanagement.database.RegisteredWatchDatabase
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsDbRepository
import com.boswelja.smartwatchextensions.core.settings.WatchSettingsRepository
import com.boswelja.smartwatchextensions.core.settings.database.WatchSettingsDatabase
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * A Koin module for providing core dependencies.
 */
val coreCommonModule = module {
    // Settings repository
    single {
        WatchSettingsDatabase(
            get {
                parametersOf(WatchSettingsDatabase.Schema, "watchsettings.db")
            }
        )
    }
    single<WatchSettingsRepository> { WatchSettingsDbRepository(get(), get(named("database"))) }

    // Registered watch repository
    single {
        RegisteredWatchDatabase(
            get {
                parametersOf(RegisteredWatchDatabase.Schema, "registeredwatches.db")
            }
        )
    }
    single<RegisteredWatchRepository> {
        RegisteredWatchDbRepository(get(), get(), get(named("database")))
    }
}
