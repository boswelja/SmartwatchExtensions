package com.boswelja.smartwatchextensions.dashboard

import com.boswelja.smartwatchextensions.dashboard.ui.DashboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * A Koin module to provide Dashboard classes.
 */
val dashboardModule = module {
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
}
