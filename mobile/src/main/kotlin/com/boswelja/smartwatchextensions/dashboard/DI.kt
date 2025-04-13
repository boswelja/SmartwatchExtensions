package com.boswelja.smartwatchextensions.dashboard

import com.boswelja.smartwatchextensions.dashboard.ui.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * A Koin module to provide Dashboard classes.
 */
val dashboardModule = module {
    viewModelOf(::DashboardViewModel)
}
