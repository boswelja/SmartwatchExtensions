package com.boswelja.smartwatchextensions.settings

import com.boswelja.smartwatchextensions.settings.database.DB_DISPATCHER
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val settingsCommonModule = DI.Module(name = "SettingsCommon") {
    bind<WatchSettingsRepository>() with singleton {
        WatchSettingsDbRepository(instance(), DB_DISPATCHER)
    }
}
