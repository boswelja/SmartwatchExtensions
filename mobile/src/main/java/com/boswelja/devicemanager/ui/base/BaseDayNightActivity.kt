package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ui.main.SettingsFragment

abstract class BaseDayNightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val nightMode = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsFragment.DAYNIGHT_MODE_KEY,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())!!.toInt()
        AppCompatDelegate.setDefaultNightMode(nightMode)
        super.onCreate(savedInstanceState)
    }
}