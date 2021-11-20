package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import com.boswelja.smartwatchextensions.appmanager.ui.AppInfoViewModel
import com.boswelja.smartwatchextensions.appmanager.ui.AppManagerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * A Koin module for providing App Manager related classes.
 */
val appManagerModule = module {
    loadKoinModules(appManagerCommonModule)

    single<WatchAppIconRepository> { WatchAppIconFsRepository(get<Context>().filesDir) }

    viewModel { AppManagerViewModel(get(), get(), get(), get()) }
    viewModel { AppInfoViewModel(get(), get(), get()) }
}
