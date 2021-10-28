package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing DnD Sync classes.
 */
val dndSyncModule = module {
    viewModel {
        DnDSyncSettingsViewModel(
            get<Context>().getSystemService(NotificationManager::class.java),
            get()
        )
    }
}
