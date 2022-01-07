package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * A Koin module for providing Battery Sync classes.
 */
val batterySyncModule = module {
    loadKoinModules(batterySyncCommonModule)
    viewModel { BatterySyncViewModel(androidApplication(), get(), get(), get(), get(), get()) }
    worker { BatterySyncWorker(get(), androidContext(), get()) }
}
