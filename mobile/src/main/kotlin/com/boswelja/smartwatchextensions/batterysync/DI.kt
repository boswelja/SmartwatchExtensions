package com.boswelja.smartwatchextensions.batterysync

import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val batterySyncUiModule = module {
    viewModel { BatterySyncViewModel(get()) }
}
