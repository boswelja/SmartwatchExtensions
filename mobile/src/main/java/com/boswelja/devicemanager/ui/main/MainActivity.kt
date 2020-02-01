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
import com.boswelja.devicemanager.EnvironmentUpdater
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity.Companion.EXTRA_PREFERENCE_KEY
import com.boswelja.devicemanager.ui.base.BaseWatchPickerActivity
import com.boswelja.devicemanager.ui.changelog.ChangelogDialogFragment
import com.boswelja.devicemanager.ui.main.appinfo.AppInfoFragment
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment
import com.boswelja.devicemanager.ui.main.extensions.ExtensionsFragment
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.ui.main.messages.MessageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseWatchPickerActivity() {

    private val coroutineScope = MainScope()

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var environmentUpdater: EnvironmentUpdater

    private val extensionsFragment = ExtensionsFragment()
    private var messagesFragment: MessageFragment? = null
    private var appSettingsFragment: AppSettingsFragment? = null
    private var appInfoFragment: AppInfoFragment? = null

    private var doFullUpdateOnServiceBound = false

    override fun onWatchManagerBound(): Boolean {
        if (doFullUpdateOnServiceBound) {
            handleFullUpdate()
            doFullUpdateOnServiceBound = false
            return false
        }
        return true
    }

    override fun getContentViewId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleUpdates()

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

    private fun handleUpdates() {
        val updater = EnvironmentUpdater(this)
        when (updater.doUpdate()) {
            EnvironmentUpdater.UPDATE_SUCCESS -> {
                ChangelogDialogFragment().show(supportFragmentManager.beginTransaction(), "ChangelogDialogFragment")
            }
            EnvironmentUpdater.NEEDS_FULL_UPDATE -> {
                environmentUpdater = updater
                doFullUpdateOnServiceBound = watchConnectionManager == null
                if (!doFullUpdateOnServiceBound) {
                    handleFullUpdate()
                }
            }
        }
    }

    private fun handleFullUpdate() {
        canUpdateConnectedWatches = false
        val dialog = MaterialAlertDialogBuilder(this).apply {
            title = getString(R.string.update_dialog_title, getString(R.string.app_name))
            setView(R.layout.common_dialog_progressbar)
            setCancelable(false)
        }.show()
        coroutineScope.launch {
            environmentUpdater.doFullUpdate(watchConnectionManager!!)
            withContext(Dispatchers.Main) {
                dialog.cancel()
                this@MainActivity.recreate()
            }
        }
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
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                MessageDatabase.open(this@MainActivity).also {
                    val messageCount = it.countMessages()
                    withContext(Dispatchers.Main) {
                        bottomNavigationView.getOrCreateBadge(R.id.messages_navigation).apply {
                            number = messageCount
                            isVisible = messageCount > 0
                        }
                    }
                    it.close()
                }
            }
        }
    }
}
