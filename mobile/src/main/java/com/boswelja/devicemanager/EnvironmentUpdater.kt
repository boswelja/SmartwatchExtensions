/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class EnvironmentUpdater(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val currentAppVersion: Int = BuildConfig.VERSION_CODE
    private val lastAppVersion: Int

    init {
        lastAppVersion = sharedPreferences.getInt(APP_VERSION_KEY, currentAppVersion)
    }

    private fun needsUpdate(): Boolean {
        return lastAppVersion < currentAppVersion
    }

    fun doUpdate(): Boolean {
        if (needsUpdate()) {
            var updateComplete = false
            sharedPreferences.edit(commit = true) {
                if (lastAppVersion < 2019090243) {
                    remove("connected_watch_name")
                    updateComplete = true
                }
                if (lastAppVersion in 2019070801..2019110999) {
                    val lastConnectedId = sharedPreferences.getString("connected_watch_id", "")
                    remove("connected_watch_id")
                    putString("last_connected_id", lastConnectedId)
                    updateComplete = true
                }
                if (lastAppVersion < 2019120600) {
                    clear()
                    updateComplete = false
                }
                putInt(APP_VERSION_KEY, lastAppVersion)
            }
            return updateComplete
        }
        return false
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
    }
}
