package com.boswelja.smartwatchextensions.settings

import android.content.Context
import com.boswelja.smartwatchextensions.core.settings.appSettingsStore
import com.boswelja.smartwatchextensions.settings.ui.WatchManagerViewModel
import com.boswelja.smartwatchextensions.settings.ui.register.RegisterWatchViewModel
import com.boswelja.smartwatchextensions.settings.ui.AppSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

/**
 * A Koin module for providing App Settings classes.
 */
val appSettingsModule = module {
    viewModelOf(::WatchManagerViewModel)
    viewModelOf(::RegisterWatchViewModel)
    viewModel { AppSettingsViewModel(get(), get(), get<Context>().appSettingsStore) }
}
