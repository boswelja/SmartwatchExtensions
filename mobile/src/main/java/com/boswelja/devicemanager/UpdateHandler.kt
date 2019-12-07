/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class UpdateHandler(private val activity: AppCompatActivity) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

    init {
        try {
            ensureUpdated()
        } catch (ignored: ClassCastException) {
            sharedPreferences.edit().remove(APP_VERSION_KEY).apply()
            ensureUpdated()
        }
    }

    @Throws(ClassCastException::class)
    private fun ensureUpdated() {
        val currentVersion = BuildConfig.VERSION_CODE
        val oldVersion = sharedPreferences.getInt(APP_VERSION_KEY, currentVersion)
        if (oldVersion < currentVersion) {
            sharedPreferences.edit(commit = true) {
                if (oldVersion < 2019090243) {
                    remove("connected_watch_name")
                }
                if (oldVersion in 2019070801..2019110999) {
                    val lastConnectedId = sharedPreferences.getString("connected_watch_id", "")
                    remove("connected_watch_id")
                    putString("last_connected_id", lastConnectedId)
                }
                if (oldVersion < 2019120600) {
                    clear()
                }
                putInt(APP_VERSION_KEY, currentVersion)
            }
        }
    }

    companion object {
        private const val APP_VERSION_KEY = "app_version"
    }
}
