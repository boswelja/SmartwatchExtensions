package com.boswelja.smartwatchextensions.settings

import android.content.Context
import com.boswelja.smartwatchextensions.settings.ui.AppSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appSettingsModule = module {
    viewModel { AppSettingsViewModel(get(), get(), get(), get<Context>().appSettingsStore) }
}
