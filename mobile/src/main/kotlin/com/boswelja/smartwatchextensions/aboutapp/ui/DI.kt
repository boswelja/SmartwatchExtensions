package com.boswelja.smartwatchextensions.aboutapp.ui

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val aboutAppModule = module {
    viewModel { AboutAppViewModel(get()) }
}
