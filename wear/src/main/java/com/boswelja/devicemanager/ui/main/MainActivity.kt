/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.main.shortcuts.AppShortcutsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        createView()
    }

    private fun createView() {
        supportFragmentManager.beginTransaction().apply {
            if (!sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false) or
                    sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)) {
                add(R.id.content, LockPhoneFragment())
                add(R.id.content, BatterySyncFragment())
            } else {
                add(R.id.content, BatterySyncFragment())
                add(R.id.content, LockPhoneFragment())
            }
            add(R.id.content, AppShortcutsFragment())
        }.also {
            it.commit()
        }
    }
}
