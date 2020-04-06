/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

abstract class BaseDayNightActivity :
        AppCompatActivity(),
        SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var sharedPreferences: SharedPreferences

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DAYNIGHT_MODE_KEY -> recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val nightMode = sharedPreferences.getString(
                DAYNIGHT_MODE_KEY,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())!!.toInt()
        AppCompatDelegate.setDefaultNightMode(nightMode)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        const val DAYNIGHT_MODE_KEY = "daynight_mode"
    }
}
