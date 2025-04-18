package com.boswelja.smartwatchextensions.settings

import android.content.Context
import com.boswelja.smartwatchextensions.core.settings.appSettingsStore
import com.boswelja.smartwatchextensions.settings.ui.AppSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing App Settings classes.
 */
val appSettingsModule = module {
    viewModel { AppSettingsViewModel(get(), get(), get<Context>().appSettingsStore) }
}
