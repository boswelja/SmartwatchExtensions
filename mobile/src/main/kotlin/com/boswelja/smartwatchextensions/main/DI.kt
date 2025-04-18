package com.boswelja.smartwatchextensions.main

import com.boswelja.smartwatchextensions.main.ui.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * A Koin module for providing main classes.
 */
val mainModule = module {
    viewModelOf(::MainViewModel)
}
