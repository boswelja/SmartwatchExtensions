package com.boswelja.smartwatchextensions.dndsync

import com.boswelja.smartwatchextensions.dndsync.ui.DnDSyncSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * A Koin module for providing DnD Sync classes.
 */
val dndSyncModule = module {
    viewModelOf(::DnDSyncSettingsViewModel)
}
