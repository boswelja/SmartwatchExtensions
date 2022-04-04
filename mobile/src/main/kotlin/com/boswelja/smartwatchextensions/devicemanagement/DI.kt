package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.smartwatchextensions.devicemanagement.ui.WatchManagerViewModel
import com.boswelja.smartwatchextensions.devicemanagement.ui.register.RegisterWatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module to provide Watch Manager classes.
 */
val watchManagerModule = module {
    viewModel { WatchManagerViewModel(get()) }
    viewModel { RegisterWatchViewModel(get()) }
}
