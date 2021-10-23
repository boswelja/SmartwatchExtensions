package com.boswelja.smartwatchextensions.managespace

import com.boswelja.smartwatchextensions.managespace.ui.ManageSpaceViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val manageSpaceModule = module {
    viewModel { ManageSpaceViewModel(get(), get(), get(), get(), Dispatchers.IO) }
}
