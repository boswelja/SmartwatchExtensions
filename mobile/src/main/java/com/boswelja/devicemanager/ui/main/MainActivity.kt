/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivityMainBinding
import com.boswelja.devicemanager.messages.database.MessageDatabase.Companion.MESSAGE_COUNT_KEY
import com.boswelja.devicemanager.ui.base.BaseWatchPickerActivity
import com.boswelja.devicemanager.ui.changelog.ChangelogDialogFragment
import com.boswelja.devicemanager.ui.main.appinfo.AppInfoFragment
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment
import com.boswelja.devicemanager.ui.main.extensions.ExtensionsFragment
import com.boswelja.devicemanager.ui.main.messages.MessageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber

class MainActivity : BaseWatchPickerActivity() {

    private lateinit var binding: ActivityMainBinding

    private val extensionsFragment = ExtensionsFragment()
    private var messagesFragment: MessageFragment? = null
    private var appSettingsFragment: AppSettingsFragment? = null
    private var appInfoFragment: AppInfoFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            setupWatchPickerSpinner(toolbarLayout.toolbar)
            bottomNavigation.setOnNavigationItemSelectedListener {
                handleNavigation(it.itemId)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart() called")
        updateMessagesBadge()
        showChangelogIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        handleNavigation(binding.bottomNavigation.selectedItemId)
    }

    /**
     * Shows the changelog if [SHOW_CHANGELOG_KEY] is true.
     */
    private fun showChangelogIfNeeded() {
        if (sharedPreferences.getBoolean(SHOW_CHANGELOG_KEY, false)) {
            Timber.i("Showing changelog")
            ChangelogDialogFragment().show(supportFragmentManager)
        }
    }

    /**
     * Handle navigation when a new item is selected in the [BottomNavigationView].
     * @param selectedItemId The ID of the selected item.
     */
    private fun handleNavigation(selectedItemId: Int): Boolean {
        Timber.d("handleNavigation() called")
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

    /**
     * Shows the [ExtensionsFragment].
     */
    private fun showExtensionsFragment() {
        Timber.i("Showing ExtensionsFragment")
        navigate(extensionsFragment)
    }

    /**
     * Shows the [MessageFragment].
     */
    private fun showMessagesFragment() {
        if (messagesFragment == null) messagesFragment = MessageFragment()
        Timber.i("Showing MessageFragment")
        navigate(messagesFragment!!)
    }

    /**
     * Shows the [AppSettingsFragment].
     */
    private fun showAppSettingsFragment() {
        if (appSettingsFragment == null) appSettingsFragment = AppSettingsFragment()
        Timber.i("Showing AppSettingsFragment")
        navigate(appSettingsFragment!!)
    }

    /**
     * Shows the [AppInfoFragment].
     */
    private fun showAppInfoFragment() {
        if (appInfoFragment == null) appInfoFragment = AppInfoFragment()
        Timber.i("Showing AppInfoFragment")
        navigate(appInfoFragment!!)
    }

    /**
     * Navigates to a given [Fragment].
     * @param fragment The [Fragment] instance to navigate to.
     */
    private fun navigate(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_holder, fragment)
                    .commit()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    /**
     * Updates the message count badge on the [BottomNavigationView].
     */
    fun updateMessagesBadge() {
        val messageCount = sharedPreferences.getInt(MESSAGE_COUNT_KEY, 0)
        Timber.i("New message badge count = $messageCount")
        val shouldShowMessageBadge = messageCount > 0
        binding.apply {
            if (shouldShowMessageBadge) {
                bottomNavigation.getOrCreateBadge(R.id.messages_navigation).apply {
                    number = messageCount
                }
            } else {
                bottomNavigation.removeBadge(R.id.messages_navigation)
            }
        }
    }

    companion object {
        const val SHOW_CHANGELOG_KEY = "should_show_changelog"
    }
}
