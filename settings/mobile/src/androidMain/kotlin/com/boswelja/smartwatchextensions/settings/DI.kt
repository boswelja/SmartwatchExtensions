package com.boswelja.smartwatchextensions.settings

import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabase
import com.boswelja.smartwatchextensions.settings.database.WatchSettingsDatabaseLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val settingsModule = DI.Module(name = "Settings") {
    import(settingsCommonModule)
    bind<WatchSettingsDatabase>() with singleton {
        WatchSettingsDatabaseLoader(instance()).createDatabase()
    }
}
