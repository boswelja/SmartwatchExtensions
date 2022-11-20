package com.boswelja.smartwatchextensions.core

import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchControllerImpl
import com.boswelja.smartwatchextensions.core.watches.selected.selectedWatchStateStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * A Koin module for providing core dependencies.
 */
val coreModule = module {
    loadKoinModules(coreCommonModule)
    single<SelectedWatchController> {
        SelectedWatchControllerImpl(androidContext().selectedWatchStateStore)
    }
}
