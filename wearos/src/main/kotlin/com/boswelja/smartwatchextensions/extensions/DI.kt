package com.boswelja.smartwatchextensions.extensions

import com.boswelja.smartwatchextensions.devicemanagement.phoneStateStore
import com.boswelja.smartwatchextensions.extensions.ui.ExtensionsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val extensionsModule = module {
    viewModel { ExtensionsViewModel(get(), get(), get(), androidContext().phoneStateStore) }
}
