package com.boswelja.smartwatchextensions.main.ui

import android.content.Context
import com.boswelja.smartwatchextensions.phoneStateStore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing Main* classes.
 */
val mainModule = module {
    viewModel {
        MainViewModel(
            get<Context>().phoneStateStore,
            get()
        )
    }
}
