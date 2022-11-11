package com.boswelja.smartwatchextensions.extensions

import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import com.boswelja.smartwatchextensions.extensions.ui.ExtensionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module to provide extension-related classes.
 */
val extensionsModule = module {
    viewModel { ExtensionsViewModel(get(), androidContext().phoneStateStore, get()) }
}
