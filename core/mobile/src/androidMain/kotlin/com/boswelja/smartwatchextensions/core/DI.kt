package com.boswelja.smartwatchextensions.core

import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchManager
import com.boswelja.smartwatchextensions.core.devicemanagement.SelectedWatchStoreManager
import com.boswelja.smartwatchextensions.core.devicemanagement.selectedWatchStateStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * A Koin module for providing core dependencies.
 */
val coreModule = module {
    loadKoinModules(coreCommonModule)
    single<SelectedWatchManager> {
        SelectedWatchStoreManager(androidContext().selectedWatchStateStore, get())
    }
}