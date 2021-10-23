package com.boswelja.smartwatchextensions.main

import com.boswelja.smartwatchextensions.main.ui.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel { MainViewModel(get()) }
}
