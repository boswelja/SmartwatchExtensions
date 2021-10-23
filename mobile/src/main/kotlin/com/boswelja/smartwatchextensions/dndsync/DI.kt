package com.boswelja.smartwatchextensions.dndsync

import android.app.NotificationManager
import android.content.Context
import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dndSyncModule = module {
    viewModel {
        DnDSyncSettingsViewModel(
            get<Context>().getSystemService(NotificationManager::class.java),
            get()
        )
    }
}
