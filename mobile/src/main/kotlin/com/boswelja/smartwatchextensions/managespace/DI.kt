package com.boswelja.smartwatchextensions.managespace

import android.content.Context
import com.boswelja.smartwatchextensions.managespace.ui.ManageSpaceViewModel
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module for providing Manage Space classes.
 */
val manageSpaceModule = module {
    viewModel {
        ManageSpaceViewModel(get(), get(), get<Context>().appSettingsStore, Dispatchers.IO)
    }
}
