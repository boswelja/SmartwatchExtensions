package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.ui.SettingsFragment

abstract class BaseDayNightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingsFragment.SWITCH_DAYNIGHT_MODE_KEY, AppCompatDelegate.MODE_NIGHT_NO))
        super.onCreate(savedInstanceState)
    }
}