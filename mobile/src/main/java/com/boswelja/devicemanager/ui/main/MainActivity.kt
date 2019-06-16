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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.UpdateHandler
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.main.messages.MessageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity :
        BaseToolbarActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MessageFragment.MESSAGE_COUNT_KEY -> {
                updateMessagesBadge()
            }
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UpdateHandler(this)

        MessageFragment.updateMessageCount(this)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            handleNavigation(it.itemId)
        }
    }

    override fun onResume() {
        super.onResume()
        handleNavigation(bottomNavigationView.selectedItemId)
        updateMessagesBadge()
    }

    private fun updateMessagesBadge() {
        val messages = sharedPreferences.getInt(MessageFragment.MESSAGE_COUNT_KEY, 0)
        if (messages > 0) {
            bottomNavigationView.showBadge(R.id.messages_navigation).apply {
                number = messages
            }
        } else {
            bottomNavigationView.removeBadge(R.id.messages_navigation)
        }
    }

    private fun handleNavigation(selectedItemId: Int) : Boolean {
        return when (selectedItemId) {
            R.id.extensions_navigation -> {
                showExtensionsFragment()
                true
            }
            R.id.messages_navigation -> {
                showMessagesFragment()
                true
            }
            R.id.settings_navigation -> {
                showAppSettingsFragment()
                true
            }
            R.id.app_info_navigation -> {
                showAppInfoFragment()
                true
            }
            else -> false
        }
    }

    private fun showExtensionsFragment() {
        val extensionsFragment = ExtensionsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, extensionsFragment).commit()
        if (intent != null) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            if (key == PreferenceKey.PHONE_LOCKING_ENABLED_KEY) {
                extensionsFragment.scrollToPreference(key)
            }
        }
    }

    private fun showMessagesFragment() {
        val messagesFragment = MessageFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, messagesFragment).commit()
    }

    private fun showAppSettingsFragment() {
        val settingsFragment = AppSettingsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()
    }

    private fun showAppInfoFragment() {
        val appInfoFragment = AppInfoFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_holder, appInfoFragment).commit()
    }
}
