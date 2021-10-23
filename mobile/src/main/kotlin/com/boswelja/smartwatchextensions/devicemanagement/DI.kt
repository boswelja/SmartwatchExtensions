package com.boswelja.smartwatchextensions.devicemanagement

import com.boswelja.smartwatchextensions.devicemanagement.ui.WatchManagerViewModel
import com.boswelja.smartwatchextensions.devicemanagement.ui.info.WatchInfoViewModel
import com.boswelja.smartwatchextensions.devicemanagement.ui.register.RegisterWatchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val watchManagerModule = module {
    viewModel { WatchManagerViewModel(get()) }
    viewModel { WatchInfoViewModel(get()) }
    viewModel { RegisterWatchViewModel(get()) }
}