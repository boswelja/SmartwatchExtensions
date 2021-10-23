package com.boswelja.smartwatchextensions.dashboard

import com.boswelja.smartwatchextensions.dashboard.ui.DashboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    viewModel { DashboardViewModel(get(), get(), get()) }
}
