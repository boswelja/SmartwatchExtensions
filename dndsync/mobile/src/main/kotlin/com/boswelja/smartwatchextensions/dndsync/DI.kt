package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * A Koin module for providing DnD Sync classes.
 */
val dndSyncModule = module {
    viewModelOf(::DnDSyncSettingsViewModel)

    factory { get<Context>().getSystemService(NotificationManager::class.java) }
}
