/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.MainApplication.Companion.SHOW_CHANGELOG_KEY
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity.Companion.EXTRA_PREFERENCE_KEY
import com.boswelja.devicemanager.ui.base.BaseWatchPickerActivity
import com.boswelja.devicemanager.ui.changelog.ChangelogDialogFragment
import com.boswelja.devicemanager.ui.main.appinfo.AppInfoFragment
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment
import com.boswelja.devicemanager.ui.main.extensions.ExtensionsFragment
import com.boswelja.devicemanager.ui.main.messages.MessageChecker
import com.boswelja.devicemanager.ui.main.messages.MessageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseWatchPickerActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    private val extensionsFragment = ExtensionsFragment()
    private var messagesFragment: MessageFragment? = null
    private var appSettingsFragment: AppSettingsFragment? = null
    private var appInfoFragment: AppInfoFragment? = null

    override fun onWatchManagerBound(): Boolean {
        return true
    }

    override fun getContentViewId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            handleNavigation(it.itemId)
        }

        if (sharedPreferences.getBoolean(SHOW_CHANGELOG_KEY, false)) {
            ChangelogDialogFragment().show(supportFragmentManager, "ChangelogDialogFragment")
        }
    }

    override fun onResume() {
        super.onResume()
        handleNavigation(bottomNavigationView.selectedItemId)
        updateMessagesBadge()
    }

    private fun handleNavigation(selectedItemId: Int): Boolean {
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
        navigate(extensionsFragment)
        if (intent != null) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            if (key == PreferenceKey.PHONE_LOCKING_ENABLED_KEY) {
                extensionsFragment.scrollToPreference(key)
            }
        }
    }

    private fun showMessagesFragment() {
        if (messagesFragment == null) messagesFragment = MessageFragment()
        navigate(messagesFragment!!)
    }

    private fun showAppSettingsFragment() {
        if (appSettingsFragment == null) appSettingsFragment = AppSettingsFragment()
        navigate(appSettingsFragment!!)
    }

    private fun showAppInfoFragment() {
        if (appInfoFragment == null) appInfoFragment = AppInfoFragment()
        navigate(appInfoFragment!!)
    }

    private fun navigate(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_holder, fragment)
                    .commit()
        } catch (_: IllegalStateException) {}
    }

    fun updateMessagesBadge() {
        val messageCount = MessageChecker.countMessages(this)
        bottomNavigationView.getOrCreateBadge(R.id.messages_navigation).apply {
            number = messageCount
            isVisible = messageCount > 0
        }
    }
}
