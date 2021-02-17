/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.ui.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import timber.log.Timber

/**
 * An [AppCompatActivity] that handles setting up night mode and recreating on night mode changed.
 */
abstract class BaseDayNightActivity :
    AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Determines whether night mode should be automatically enabled/disabled when DAYNIGHT_MODE_KEY
     * changes. Defaults to true.
     */
    internal var shouldAutoSwitchNightMode = true

    lateinit var sharedPreferences: SharedPreferences

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DAYNIGHT_MODE_KEY -> {
                if (shouldAutoSwitchNightMode) {
                    Timber.i("$DAYNIGHT_MODE_KEY changed, recreating")
                    recreate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setNightMode()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Sets the default night mode state for the app. Fallback to
     * [AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM] if any problem occurs.
     */
    private fun setNightMode() {
        Timber.d("setNightMode() called")
        val nightMode =
            sharedPreferences
                .getString(DAYNIGHT_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
                ?.toInt()
        AppCompatDelegate.setDefaultNightMode(
            nightMode ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }

    companion object {
        const val DAYNIGHT_MODE_KEY = "daynight_mode"
    }
}
