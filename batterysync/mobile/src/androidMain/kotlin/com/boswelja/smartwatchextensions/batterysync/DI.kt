package com.boswelja.smartwatchextensions.batterysync

import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncViewModel
import com.boswelja.smartwatchextensions.batterysync.ui.phonebatterynoti.PhoneBatteryNotiSettingsViewModel
import com.boswelja.smartwatchextensions.batterysync.ui.watchbatterynoti.WatchBatteryNotiSettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * A Koin module for providing Battery Sync classes.
 */
val batterySyncModule = module {
    loadKoinModules(batterySyncCommonModule)
    single<BatterySyncNotificationHandler> {
        MobileBatterySyncNotificationHandler(get(), get(), androidContext(), androidContext().getSystemService()!!)
    }
    viewModel { BatterySyncViewModel(androidApplication(), get(), get(), get(), get(), get()) }
    viewModel { PhoneBatteryNotiSettingsViewModel(get(), get(), get()) }
    viewModel { WatchBatteryNotiSettingsViewModel(get(), get()) }
}
