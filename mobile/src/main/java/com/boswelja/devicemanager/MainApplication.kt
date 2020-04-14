/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.Application
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.updater.Result
import com.boswelja.devicemanager.updater.Updater

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
