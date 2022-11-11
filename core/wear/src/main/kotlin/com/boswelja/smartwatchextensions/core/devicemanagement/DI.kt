package com.boswelja.smartwatchextensions.core.devicemanagement

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val deviceManagementModule = module {
    single { androidContext().phoneStateStore }
}