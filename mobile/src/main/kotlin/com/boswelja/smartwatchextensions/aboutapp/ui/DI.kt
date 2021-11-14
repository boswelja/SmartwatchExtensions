package com.boswelja.smartwatchextensions.aboutapp.ui

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module to provide About App classes.
 */
val aboutAppModule = module {
    viewModel { AboutAppViewModel(get(), get()) }
}
