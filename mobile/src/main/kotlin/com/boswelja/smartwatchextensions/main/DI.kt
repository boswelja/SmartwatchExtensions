package com.boswelja.smartwatchextensions.main

import com.boswelja.smartwatchextensions.main.ui.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing main classes.
 */
val mainModule = module {
    viewModel { MainViewModel(get()) }
}
