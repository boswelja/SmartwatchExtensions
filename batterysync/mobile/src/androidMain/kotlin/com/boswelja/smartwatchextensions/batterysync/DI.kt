package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * A Koin module for providing Battery Sync UI classes.
 */
val batterySyncModule = module {
    loadKoinModules(batterySyncCommonModule)
    viewModel { BatterySyncViewModel(get(), get(), get(), get(), get(), get()) }
}
