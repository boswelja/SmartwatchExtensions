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
import androidx.core.widget.NestedScrollView
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.UpdateHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.main.messages.MessageFragment

class MainActivity : BaseToolbarActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun getContentViewId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UpdateHandler(this)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        findViewById<NestedScrollView>(R.id.scroll_view).apply {
            setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
                elevateToolbar(scrollY != 0)
            }
        }

        showMessageFragment()
        showSettingsFragment()
    }

    private fun showMessageFragment() {
        val messageFragment = MessageFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.message_fragment_holder, messageFragment)
                .commit()
    }

    private fun showSettingsFragment() {
        val settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.settings_fragment_holder, settingsFragment).commit()
        if (intent != null) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            if (key == PreferenceKey.PHONE_LOCKING_ENABLED_KEY ||
                    key == SettingsFragment.OPEN_NOTI_SETTINGS_KEY ||
                    key == SettingsFragment.DAYNIGHT_MODE_KEY) {
                settingsFragment.scrollToPreference(key)
            }
        }
    }
}
